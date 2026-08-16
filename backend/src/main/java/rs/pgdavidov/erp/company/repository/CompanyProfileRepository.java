package rs.pgdavidov.erp.company.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.pgdavidov.erp.company.entity.CompanyProfile;

import java.util.UUID;

public interface CompanyProfileRepository
        extends JpaRepository<CompanyProfile, UUID> {
}