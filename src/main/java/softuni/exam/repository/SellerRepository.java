package softuni.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import softuni.exam.models.entity.Seller;

import java.util.Optional;

//ToDo
@Repository
public interface SellerRepository extends JpaRepository<Seller,Integer> {
    Optional<Seller> findByEmail(String email);
}
