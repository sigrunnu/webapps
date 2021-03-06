package socialnetwork.model;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface PublicationRepository extends CrudRepository<Publication, Integer> {
    List<Publication> findByUserOrderByTimestampDesc(User user);
    List<Publication> findFirst10ByRestrictedIsFalseOrderByTimestampDesc();
    List<Publication> findByUserAndRestrictedIsFalseOrderByTimestampDesc(User user);
    List<Publication> findFirst20ByUserInOrderByTimestampDesc(List<User> friends);
    List<Publication> deleteById(int id);
}