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

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
@RequestMapping("/")
public class MainController {

    @GetMapping(path = "/")
    public String mainView(Model model, Principal principal, Publication publication) {        
        User user = userRepository.findByEmail(principal.getName());

        model.addAttribute("publications", publicationRepository.findFirst10ByRestrictedIsFalseOrderByTimestampDesc());
        model.addAttribute("user", user);
        return "main_view";
    }

    @GetMapping(path = "/user/{userId}")
    public String userView(@PathVariable int userId, Model model) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        User user = userOpt.get();
        model.addAttribute("user", user);
        model.addAttribute("publications", publicationRepository.findByUserOrderByTimestampDesc(user));
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

   @GetMapping(path = "/editDesc")
   public String editDesc() {
       return "description";
   }  

   @PostMapping(path = "/postDesc")
   public String postDescription(
                                    BindingResult bindingResult,
                                    Principal principal, 
                                    @RequestParam String description) {
        if (bindingResult.hasErrors()) {
            return "redirect:/user/{userId}";
        }
        User user = userRepository.findByEmail(principal.getName());
        user.setDescription(description);
        userRepository.save(user);
        return "redirect:/user/{userId}";
    } 


}
