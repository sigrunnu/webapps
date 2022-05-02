package socialnetwork.controllers;

import java.util.Date;
import java.time.LocalDate;
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
import java.text.SimpleDateFormat;

import socialnetwork.services.FriendshipRequestException;
import socialnetwork.services.FriendshipRequestService;
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
import java.util.ArrayList;



@Controller
@RequestMapping("/")
public class MainController {

    @Autowired
    FriendshipRequestRepository friendshipRequestRepository; 
    @GetMapping(path = "/")
    public String mainView(Model model, Principal principal, Publication publication) {        
        User user = userRepository.findByEmail(principal.getName());
        model.addAttribute("publications", publicationRepository.findFirst20ByUserInOrderByTimestampDesc(user.getFriends()));
        List <User> sortedUsers = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (User user2 : user.getFriends()) {
            if(user2.getBirthdate() != null){
            LocalDate birthday = LocalDate.parse(user2.getBirthdate().toString().substring(0,10));
            if(birthday.getMonth() == now.getMonth()){
                if(now.getDayOfMonth()<birthday.getDayOfMonth() ){
                sortedUsers.add(user2);
                }
             }
             System.out.print(now.getDayOfYear());
             System.out.print("dato" + birthday.getDayOfYear());
        }
    }
        model.addAttribute("dates", sortedUsers);
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
        model.addAttribute("sessionUser", sessionUser);
        if(user.getFriends().contains(sessionUser) || user == sessionUser ){
            model.addAttribute("publications", publicationRepository.findByUserOrderByTimestampDesc(user));
        } else {
            model.addAttribute("publications", publicationRepository.findByUserAndRestrictedIsFalseOrderByTimestampDesc(user));
        }
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
   public String postDescription(Principal principal, @RequestParam String description, @RequestParam String birthdate){ 
        User user = userRepository.findByEmail(principal.getName());
        int userId = user.getId();
        System.out.print(birthdate);
        try {
            Date date =  new SimpleDateFormat("yyyy-MM-dd").parse(birthdate);
            user.setBirthdate(date);
        }
        catch (Exception e){  
            return "redirect:/";
        }
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

    @GetMapping(path = "/deletePublication")
    public String deletePublication(@RequestParam int publicationId, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        publicationRepository.deleteById(publicationId);
        return "redirect:/user/" + user.getId();
    } 
    
  
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
            return "redirect:/";
        }
        catch(Exception e){
            return "redirect:/";
        }
    }
}