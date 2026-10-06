
package com.example.contactform;


import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ContactController {

    private final ContactRepository contactRepository;

    public ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    // Existing contact form submission
    @PostMapping("/contact")
    public String saveContact(@ModelAttribute Contact contact) {
        contactRepository.save(contact);
        return "redirect:/success.html";
    }

    // Get contacts with pagination and sorting
    @GetMapping("/contacts")
    @ResponseBody
    public Page<Contact> getAllContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid page or page size");
        }

        String[] sortParts = sort.split(",", 2);
        String sortField = sortParts[0];

        Sort.Direction direction = Sort.Direction.ASC;

        if (sortParts.length == 2
                && sortParts[1].equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        if (!sortField.equals("name")
                && !sortField.equals("email")
                && !sortField.equals("id")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sort by name, email, or id only");
        }

        Pageable pageable = PageRequest.of(
                page, size, Sort.by(direction, sortField));

        return contactRepository.findAll(pageable);
    }

    // Create a contact using the management API
    @PostMapping("/contacts")
    @ResponseBody
    public ResponseEntity<Contact> createContact(
            @Valid @RequestBody Contact contact) {

        contact.setId(null);

        Contact savedContact = contactRepository.save(contact);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedContact);
    }

    // Update an existing contact
    @PutMapping("/contacts/{id}")
    @ResponseBody
    public Contact updateContact(
            @PathVariable Long id,
            @Valid @RequestBody Contact updatedContact) {

        Contact existingContact = contactRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Contact not found"));

        existingContact.setName(updatedContact.getName());
        existingContact.setEmail(updatedContact.getEmail());
        existingContact.setMessage(updatedContact.getMessage());

        return contactRepository.save(existingContact);
    }

    // Delete a contact
    @DeleteMapping("/contacts/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {

        if (!contactRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Contact not found");
        }

        contactRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}