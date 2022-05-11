package socialnetwork.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import java.util.List;
import java.util.Date;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false, length = 64)
    @NotBlank
    @Size(max = 64)
    private String name;

    @Column(unique = true, nullable = false, length = 64)
    @Email
    @NotBlank
    @Size(max = 64)
    private String email;

    @Lob
    private String description;

    @Column(nullable = true)
    private Date birthdate;

    @Column(nullable = false)
    @NotBlank
    private String password;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private List<Publication> publications;

    @ManyToMany
    @JoinTable(name = "friends", joinColumns = @JoinColumn(name = "subject_id"), inverseJoinColumns = @JoinColumn(name = "friend_id"))
    private List<User> friends;

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public int getNumberOfPublications() {
        List<Publication> publications = getPublications();
        return publications.size();
    }

    public int getNumberOfRestrictedPublications(){
        List<Publication> publications = getPublications();
        int counter = 0;
        for (Publication publication : publications){
            if (publication.isRestricted()){
                counter++;
            } 
        } 
        return counter;
    } 

    public Date getBirthdate() {
        return birthdate;
    }

    public String getFormattedBirthdate() {
        if(birthdate != null) {
        String formattedBirthDate = birthdate.toString().substring(0,10);
        return formattedBirthDate;
    }
        return null;
        
    }


    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public List<User> getFriends() {
        return friends;
    }

    public int getNumberOfFriends() {
        List<User> friends = getFriends();
        return friends.size();
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    public void setFriend(User friend) {
        this.friends.add(friend);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User: " + name + " <" + email + ">";
    }
}