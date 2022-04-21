package socialnetwork.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import socialnetwork.model.Publication;
import socialnetwork.model.PublicationRepository;
import socialnetwork.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import socialnetwork.services.UserService;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import javax.validation.Valid;
import org.springframework.validation.BindingResult;
import socialnetwork.model.UserRepository;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/")
public class MainController {

    @GetMapping(path = "/")
    public String mainView(Model model, Principal principal, Publication publication) {        
        User user = userRepository.findByEmail(principal.getName());
        
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
        model.addAttribute("user", user);

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
    public String register(User user) {
        return "register";
    }

    // A añadir en el bloque de atributos:
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // Código alternativo para el método "register" con mensaje POST:
    @PostMapping(path = "/register")
    public String register(@Valid @ModelAttribute("user") User user,
                        BindingResult bindingResult,
                        @RequestParam String passwordRepeat) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return "redirect:register?duplicate_email";
        }
        if (user.getPassword().equals(passwordRepeat)) {
            userService.register(user);
        } else {
            return "redirect:register?passwords";
        }
        return "redirect:login?registered";
    }

    @Autowired
    private PublicationRepository publicationRepository;

    @PostMapping(path = "/post")
    public String postPublication(@Valid @ModelAttribute("publication") Publication publication,
                              BindingResult bindingResult,
                              Principal principal) {
        if (bindingResult.hasErrors()) {
            return "redirect:/";
        }
        User user = userRepository.findByEmail(principal.getName());
        publication.setUser(user);
        publication.setTimestamp(new Date());
        publicationRepository.save(publication);
        return "redirect:/";
    }

}
