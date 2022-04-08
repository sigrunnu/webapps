package socialnetwork.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import socialnetwork.model.Publication;
import socialnetwork.model.User;

@Controller
@RequestMapping("/")
public class MainController {

    @GetMapping(path = "/")
    public String mainView(Model model) {
        User profileUser = new User();
        profileUser.setName("Mary Jones");
        profileUser.setDescription("Addicted to social networks");
        List<Publication> publications = new ArrayList<Publication>();
        User userJane = new User();
        userJane.setEmail("jane@example.com");
        userJane.setName("Jane Doe");
        userJane.setDescription("I love dogs!");
        User userJohn = new User();
        userJohn.setEmail("john@excample.com");
        userJohn.setName("John Doe");
        userJohn.setDescription("Professional couch potato");
        Publication pub1 = new Publication();
        pub1.setUser(userJane);
        pub1.setText("I've published a new tutorial about how to create a Spring MVC app!!!");
        pub1.setRestricted(false);
        pub1.setTimestamp(new Date());
        Publication pub2 = new Publication();
        pub2.setUser(userJohn);
        pub2.setText("Watching TV for 8 hours in a row. It's a new record!!!");
        pub2.setRestricted(true);
        pub2.setTimestamp(new Date());
        Publication pub3 = new Publication();
        pub3.setUser(userJohn);
        pub3.setText("Just lifted 77,5 kg in squats #lol");
        pub3.setRestricted(true);
        pub3.setTimestamp(new Date());
        publications.add(pub1);
        publications.add(pub2);
        publications.add(pub3);
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("publications", publications);
        return "main_view";
    }

    @GetMapping(path = "/user")
    public String userView(Model model) {
        User user = new User();
        user.setName("Sigrun Nummedal");
        user.setDescription("Addicted to social networks");

        List<Publication> publications = new ArrayList<Publication>();
        model.addAttribute("profileUser", user);
        Publication pub1 = new Publication();
        pub1.setUser(user);
        pub1.setText("Hey this is my first post.");
        pub1.setRestricted(true);
        pub1.setTimestamp(new Date());
        publications.add(pub1);
        Publication pub2 = new Publication();
        pub2.setUser(user);
        pub2.setText("Hey this is my second post.");
        pub2.setRestricted(true);
        pub2.setTimestamp(new Date());
        publications.add(pub2);
        model.addAttribute("publications", publications);

        return "user_view";

    }

    @GetMapping(path = "/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping(path = "/register")
    public String registerForm() {
        return "register";
    }
}
