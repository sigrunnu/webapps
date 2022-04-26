package socialnetwork.controllers;

import java.util.Date;
import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import socialnetwork.model.FriendshipRequest;
import socialnetwork.model.FriendshipRequestRepository;
import socialnetwork.model.Publication;
import socialnetwork.model.PublicationRepository;
import socialnetwork.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import socialnetwork.services.FriendshipRequestException;
import socialnetwork.services.FriendshipRequestService;
import socialnetwork.services.FriendshipRequestServiceImpl;
import socialnetwork.services.UserService;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import javax.validation.Valid;
import org.springframework.validation.BindingResult;
import socialnetwork.model.UserRepository;
import socialnetwork.model.FriendshipRequest.State;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;


@Controller
@RequestMapping("/")
public class MainController {

    @Autowired
    FriendshipRequestRepository friendshipRequestRepository; 
    @GetMapping(path = "/")
    public String mainView(Model model, Principal principal, Publication publication) {        
        User user = userRepository.findByEmail(principal.getName());

        model.addAttribute("publications", publicationRepository.findFirst10ByRestrictedIsFalseOrderByTimestampDesc());
        model.addAttribute("user", user);
        model.addAttribute("friendshipRequests", friendshipRequestRepository.findByReceiverAndState(user, FriendshipRequest.State.OPEN));
        return "main_view";
    }

  
   


    @GetMapping(path = "/user/{userId}")
    public String userView(@PathVariable int userId, Model model, Principal  principal) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        User sessionUser = userRepository.findByEmail(principal.getName());
        User user = userOpt.get();
        model.addAttribute("user", user);
        model.addAttribute("publications", publicationRepository.findByUserOrderByTimestampDesc(user));
        List<FriendshipRequest> requests = friendshipRequestRepository.findBySenderAndReceiverAndState(sessionUser, user, FriendshipRequest.State.OPEN);
        if (!requests.isEmpty()) {
            model.addAttribute("request", requests.get(0));
        } else {
            requests = friendshipRequestRepository.findBySenderAndReceiverAndState(user, sessionUser, FriendshipRequest.State.OPEN);
            if (!requests.isEmpty()) {
                model.addAttribute("request", requests.get(0));
            } else {
                model.addAttribute("request", null);
            }
        }
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
   public String editDesc(Principal principal, Model model) {
       User user = userRepository.findByEmail(principal.getName());
       model.addAttribute("user", user);
       return "description";
   }  

   @PostMapping(path = "/postDesc")
   public String postDescription(Principal principal, @RequestParam String description) {
        
        User user = userRepository.findByEmail(principal.getName());
        int userId = user.getId();
        user.setDescription(description);
        userRepository.save(user);
        
        return "redirect:/user/" + Integer.toString(userId);
    } 

   @Autowired
   private FriendshipRequestService friendshipRequestService; 

    @PostMapping(path = "/requestFriendship")
    public String requestFriendship(@RequestParam int userId, Principal principal) throws FriendshipRequestException{
        try{
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return "redirect:/";
            }
            User sender = userRepository.findByEmail(principal.getName());
            User receiver = userOpt.get();
            friendshipRequestService.createFriendshipRequest(sender, receiver);
            return "redirect:/user/" + receiver.getId();
        } catch(Exception e) {
            return "redirect:/";

        }
    }

    /*
    Get the logged in user and friend request items.

    Invoke the corresponding service method to accept or decline the request.

    If the request is accepted, redirect the user to the profile view of the user whose friend you just accepted.

    If it is declined or an error occurs, redirect to the main page.
    Restart the app, reply to any pending friend requests, and check the database to make sure the code is working properly:
    */
    @PostMapping(path = "/answerFriendshipRequest")
    public String answerFriendshipRequest(@RequestParam int requestId, @RequestParam String action, Principal principal) throws FriendshipRequestException {
        try{
            User receiver = userRepository.findByEmail(principal.getName());
            List<FriendshipRequest> requests = friendshipRequestRepository.findByReceiverAndState(receiver, State.OPEN);
            FriendshipRequest request = null;
            for (FriendshipRequest friendshipRequest : requests) {
                if(friendshipRequest.getId() == requestId){
                    request = friendshipRequest;
                }
            }
           if(action.equals("Accept") && request != null){
               friendshipRequestService.acceptFriendshipRequest(request, receiver);
               User sender = request.getSender();
               int id = sender.getId();
               return "redirect:/user/" + Integer.toString(id);
            }else if (action.equals("Decline") && request != null){
                friendshipRequestService.declineFriendshipRequest(request, receiver);
                
            }
            System.out.print(action);
            return "redirect:/";
        }
        catch(Exception e){
            return "redirect:/hei";
        }
    }
}