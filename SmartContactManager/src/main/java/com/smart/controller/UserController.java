package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal)
	{
		String userName = principal.getName();
		System.out.println(userName);
		
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER "+user);
		
		model.addAttribute("user", user);
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	//add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form handler
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute("contact") @Valid Contact contact,
								BindingResult bindingResult,
								@RequestParam("profileImage")MultipartFile file,
								Model model,Principal principal,
								HttpSession session)
	{
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		//processing and uploading file
		if(file.isEmpty())
		{
			System.out.println("File is Empty");
			contact.setImage("contact.png");
			
		}else {
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("static/image").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image uploaded successfully");
		}
		
		contact.setUser(user);
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("Data added to database successfull ");
		
		//message success
		session.setAttribute("message", new Message("Your contact added !! Add more..", "success"));
		
		}catch (Exception e) {
			
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong", "danger"));
		}
		return "normal/add_contact_form";
	}
	
	//show contact handler
	//per page = 4
	//current page = 0[page]
	@RequestMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page, Model model, Principal principal)
	{
		model.addAttribute("title", "Show Contact");
		
		//send contact list of logged in user
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		PageRequest pageable = PageRequest.of(page, 3);
		
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
		
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId,
										Model model,Principal principal)
	{
		System.out.println("cId "+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact modelContact = contactOptional.get();
		
		String name = principal.getName();
		User user = userRepository.getUserByUserName(name);
		model.addAttribute("title", modelContact.getName());
		
		if(user.getId()==modelContact.getUser().getId())
		{
			model.addAttribute("modelContact", modelContact);
		}	
		
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cId}")
	@Transactional
	public String deleteContact(@PathVariable("cId")Integer cId,
								Model model,
								Principal principal,
								HttpSession session)
	{
		
		Contact contact = this.contactRepository.findById(cId).get();
		System.out.println("CID "+cId);
		//check
		
//		contact.setUser(null);
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("contact deleted successfully !!", "success"));
		
		return "redirect:/user/show_contacts/0";
		
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid")Integer cid, Model model)
	{
		model.addAttribute("title", "Update Contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,
								@RequestParam("profileImage")MultipartFile file,
								Model model,
								HttpSession session,
								Principal principal)
	{
		
		try {
			//old contact details
			Contact oldContact = this.contactRepository.findById(contact.getCid()).get();
			
			
			//image..
			if(!file.isEmpty())
			{
				//file rewrite
				//delete old photo
				File deleteFile = new ClassPathResource("static/image").getFile();
				File file1 = new File(deleteFile, oldContact.getImage());
				file1.delete();
				
				
				//update new photo
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}else {
				
				contact.setImage(oldContact.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated ", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("CONTACT "+contact.getName());
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title", "Profile");
		return "normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings()
	{
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword")String oldPassword,
								@RequestParam("newPassword")String newPassword,
								Principal principal,
								HttpSession session)
	{
		System.out.println("oldPassword "+oldPassword);
		System.out.println("newPassword "+newPassword);
		
		String name = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(name);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			
			session.setAttribute("message", new Message("your password changed successfully", "success"));
			
		}else {
			
			session.setAttribute("message", new Message("Wrong old Password !!", "danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
}
