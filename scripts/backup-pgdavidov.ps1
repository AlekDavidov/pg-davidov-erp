$ErrorActionPreference = "Stop"

# ============================================================
# PGDavididovERP - Automated Backup
# ============================================================

$ProjectRoot = Split-Path -Parent $PSScriptRoot

$PostgresService = "postgres"
$PostgresContainer = "pg-davidov-erp-postgres-1"

$DatabaseName = "pg_davidov_erp"
$DatabaseUser = "pgdavidov"

$DocumentVolume = "pg-davidov-erp_document_storage"

$RcloneRemote = "pgdavidov-drive"
$RemoteBackupRoot = "PGDavidovERP/Backups/daily"

$RetentionDays = 30

$Timestamp = Get-Date -Format "yyyy-MM-dd_HHmm"

$LocalBackupRoot = Join-Path $ProjectRoot "backups\auto"
$LocalBackupDir = Join-Path $LocalBackupRoot $Timestamp

$LogDirectory = Join-Path $ProjectRoot "logs"
$LogFile = Join-Path $LogDirectory "backup.log"

$CredentialFile = Join-Path `
    $env:USERPROFILE `
    ".pgdavidov-backup-credential.xml"

$DbFileName = "pg-davidov-erp.dump"
$DocumentsFileName = "documents.tar.gz"

$LocalDbFile = Join-Path $LocalBackupDir $DbFileName
$LocalDocumentsFile = Join-Path $LocalBackupDir $DocumentsFileName

$ContainerDbFile = "/tmp/pg-davidov-erp-backup.dump"

$RemoteBackupDir = "${RcloneRemote}:${RemoteBackupRoot}/${Timestamp}"


function Write-BackupLog {
    param (
        [string]$Status,
        [string]$Message
    )

    if (-not (Test-Path $LogDirectory)) {
        New-Item `
            -ItemType Directory `
            -Force `
            -Path $LogDirectory |
        Out-Null
    }

    $LogTimestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

    Add-Content `
        -Path $LogFile `
        -Value "$LogTimestamp | $Status | $Message"
}


function Send-BackupFailureEmail {
    param (
        [string]$ErrorMessage
    )

    if (-not (Test-Path $CredentialFile)) {
        Write-BackupLog `
            -Status "EMAIL_FAILED" `
            -Message "Credential file was not found."

        return
    }

    try {
        $Credential = Import-Clixml -Path $CredentialFile

        $EmailAddress = $Credential.UserName

        $Subject = "PGDavidovERP BACKUP FAILED - $(Get-Date -Format 'yyyy-MM-dd HH:mm')"

        $Body = @"
PGDavidovERP automatic backup failed.

Time:
$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

Computer:
$env:COMPUTERNAME

Error:
$ErrorMessage

Local log:
$LogFile

Please check the laptop, Docker containers and Google Drive connection.
"@

        $MailMessage = New-Object System.Net.Mail.MailMessage

        $MailMessage.From = $EmailAddress
        $MailMessage.To.Add($EmailAddress)
        $MailMessage.Subject = $Subject
        $MailMessage.Body = $Body

        $SmtpClient = New-Object System.Net.Mail.SmtpClient(
            "smtp.gmail.com",
            587
        )

        $SmtpClient.EnableSsl = $true

        $SmtpClient.Credentials = New-Object `
            System.Net.NetworkCredential(
                $Credential.UserName,
                $Credential.GetNetworkCredential().Password
            )

        $SmtpClient.Send($MailMessage)

        $MailMessage.Dispose()
        $SmtpClient.Dispose()

        Write-BackupLog `
            -Status "EMAIL_SENT" `
            -Message "Failure notification sent to $EmailAddress"
    }
    catch {
        Write-BackupLog `
            -Status "EMAIL_FAILED" `
            -Message $_.Exception.Message
    }
}


Write-Host ""
Write-Host "============================================"
Write-Host "PGDavidovERP backup started"
Write-Host "Timestamp: $Timestamp"
Write-Host "============================================"
Write-Host ""

try {

    # --------------------------------------------------------
    # Check required commands
    # --------------------------------------------------------

    Write-Host "[1/8] Checking required commands..."

    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw "Docker command was not found."
    }

    if (-not (Get-Command rclone -ErrorAction SilentlyContinue)) {
        throw "rclone command was not found."
    }

    # --------------------------------------------------------
    # Check PostgreSQL container
    # --------------------------------------------------------

    Write-Host "[2/8] Checking PostgreSQL container..."

    Push-Location $ProjectRoot

    try {
        $PostgresRunning = docker compose ps `
            --status running `
            --services |
        Select-String "^${PostgresService}$"

        if (-not $PostgresRunning) {
            throw "PostgreSQL container is not running."
        }
    }
    finally {
        Pop-Location
    }

    # --------------------------------------------------------
    # Create local backup directory
    # --------------------------------------------------------

    Write-Host "[3/8] Creating local backup directory..."

    New-Item `
        -ItemType Directory `
        -Force `
        -Path $LocalBackupDir |
    Out-Null

    # --------------------------------------------------------
    # PostgreSQL backup
    # --------------------------------------------------------

    Write-Host "[4/8] Creating PostgreSQL backup..."

    Push-Location $ProjectRoot

    try {
        docker compose exec -T $PostgresService `
            rm -f $ContainerDbFile

        if ($LASTEXITCODE -ne 0) {
            throw "Could not remove old temporary database dump."
        }

        docker compose exec -T $PostgresService `
            pg_dump `
            -U $DatabaseUser `
            -Fc `
            -f $ContainerDbFile `
            $DatabaseName

        if ($LASTEXITCODE -ne 0) {
            throw "PostgreSQL backup failed."
        }
    }
    finally {
        Pop-Location
    }

    docker cp `
        "${PostgresContainer}:${ContainerDbFile}" `
        $LocalDbFile

    if ($LASTEXITCODE -ne 0) {
        throw "Could not copy PostgreSQL dump from container."
    }

    if (
        -not (Test-Path $LocalDbFile) -or
        (Get-Item $LocalDbFile).Length -eq 0
    ) {
        throw "PostgreSQL dump is missing or empty."
    }

    # --------------------------------------------------------
    # Document storage backup
    # --------------------------------------------------------

    Write-Host "[5/8] Creating document storage backup..."

    docker run --rm `
        --mount "type=volume,src=$DocumentVolume,dst=/data,readonly" `
        --mount "type=bind,src=$LocalBackupDir,dst=/backup" `
        alpine `
        tar -czf "/backup/$DocumentsFileName" -C /data .

    if ($LASTEXITCODE -ne 0) {
        throw "Document storage backup failed."
    }

    if (-not (Test-Path $LocalDocumentsFile)) {
        throw "Document storage archive was not created."
    }

    # --------------------------------------------------------
    # Upload to Google Drive
    # --------------------------------------------------------

    Write-Host "[6/8] Uploading backup to Google Drive..."

    rclone copy `
        $LocalBackupDir `
        $RemoteBackupDir

    if ($LASTEXITCODE -ne 0) {
        throw "Google Drive upload failed."
    }

    # --------------------------------------------------------
    # Verify uploaded files
    # --------------------------------------------------------

    Write-Host "[7/8] Verifying Google Drive backup..."

    $RemoteFiles = rclone lsf $RemoteBackupDir

    if ($LASTEXITCODE -ne 0) {
        throw "Could not verify Google Drive backup."
    }

    if ($RemoteFiles -notcontains $DbFileName) {
        throw "Database dump is missing from Google Drive."
    }

    if ($RemoteFiles -notcontains $DocumentsFileName) {
        throw "Document storage archive is missing from Google Drive."
    }

    # --------------------------------------------------------
    # Retention and local cleanup
    # --------------------------------------------------------

    Write-Host "[8/8] Applying retention and cleaning local files..."

    rclone delete `
        "${RcloneRemote}:${RemoteBackupRoot}" `
        --min-age "${RetentionDays}d"

    if ($LASTEXITCODE -ne 0) {
        throw "Google Drive retention cleanup failed."
    }

    rclone rmdirs `
        "${RcloneRemote}:${RemoteBackupRoot}" `
        --leave-root

    if ($LASTEXITCODE -ne 0) {
        throw "Could not remove empty old backup directories."
    }

    Remove-Item `
        -Path $LocalBackupDir `
        -Recurse `
        -Force

    Push-Location $ProjectRoot

    try {
        docker compose exec -T $PostgresService `
            rm -f $ContainerDbFile
    }
    finally {
        Pop-Location
    }

    Write-BackupLog `
        -Status "SUCCESS" `
        -Message $RemoteBackupDir

    Write-Host ""
    Write-Host "============================================"
    Write-Host "BACKUP SUCCESSFUL"
    Write-Host "Google Drive: $RemoteBackupDir"
    Write-Host "Log: $LogFile"
    Write-Host "============================================"
    Write-Host ""
}
catch {

    $ErrorMessage = $_.Exception.Message

    Write-BackupLog `
        -Status "FAILED" `
        -Message $ErrorMessage

    Send-BackupFailureEmail `
        -ErrorMessage $ErrorMessage

    Write-Host ""
    Write-Host "============================================"
    Write-Host "BACKUP FAILED"
    Write-Host $ErrorMessage
    Write-Host "Log: $LogFile"
    Write-Host "============================================"
    Write-Host ""

    exit 1
}