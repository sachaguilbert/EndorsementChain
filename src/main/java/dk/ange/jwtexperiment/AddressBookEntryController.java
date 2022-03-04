package dk.ange.jwtexperiment;

import dk.ange.jwtexperiment.AddressBookEntry;
import dk.ange.jwtexperiment.AddressBookEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1")
public class AddressBookEntryController {
    @Autowired
    AddressBookEntryRepository addressBookEntryRepo;

    @GetMapping("/address-book-entries/{AddressBookEntryId}")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<AddressBookEntry> getAddressBookEntry(@PathVariable Integer AddressBookEntryId){
        final Optional<AddressBookEntry> addressBookEntry = addressBookEntryRepo.findById(AddressBookEntryId);
        return new ResponseEntity<AddressBookEntry>(addressBookEntry.get(), HttpStatus.OK);
    }

    @GetMapping("/address-book-entries")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<List<AddressBookEntry>> getAddressBookEntries(@RequestParam(required = false) String thumbprint) {
        final List<AddressBookEntry> addressBookEntries = StringUtils.isEmpty(thumbprint)?
                                                          addressBookEntryRepo.findAll() :
                                                          addressBookEntryRepo.findByThumbprint(thumbprint);
        return new ResponseEntity<List<AddressBookEntry>>(addressBookEntries, HttpStatus.OK);
    }
}
