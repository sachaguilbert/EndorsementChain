package dk.ange.jwtexperiment;

import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

// test with curl -v -k -X POST -H "Content-Type: application/json" -d
// '{"payload":"eyJ0ZXN0IjoidGVzdFBheWxvYWQifQ","signatures":[{"protected": "eyJhbGciOiJSUzI1NiJ9",
// "signature":"IRMQENi4nJyp4er2LmZq3ivwoAjqa1uUkSBKFIX7ATndFF5ivnt-m8uApHO4kfIFOrW7w2Ezmlg3QdmaXlS9DhN0nUk_hGI3amEjkKd0BWYCB8vfUbUv0XGjQip78AI4z1PrFRNidm7-jPDm5Iq0SZnjKjCNS5Q15fokXZc8u0A"}]}' https://localhost:8443/api/v1/transport-document-transfers

@RestController
@RequestMapping("/api/v1")
public class TransferBlockController {

  public static final String API_PATH = "/transferblocks";

  @Value("${spring.application.api-path}")
  private String contextPath;

  @Autowired TransferBlockService transferBlockService;

  @GetMapping(API_PATH + "/{transferBlockId}")
  @CrossOrigin(origins = "*")
  @ResponseBody
  public ResponseEntity<TransferBlock> getTransferBlock(@PathVariable String transferBlockId) {
    return transferBlockService
        .findById(transferBlockId)
        .map(transferBlock -> ResponseEntity.ok().body(transferBlock))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping(
      value = API_PATH,
      consumes = {"application/json"},
      produces = {"application/json"})
  @ResponseBody
  public ResponseEntity<TransferBlock> addTransferBlock(
      @RequestBody TransferBlockRequest transferBlockRequest, UriComponentsBuilder builder)
      throws java.text.ParseException, IOException, com.nimbusds.jose.JOSEException,
          NoSuchAlgorithmException {
    String hash = transferBlockService.save(transferBlockRequest);
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(
        builder.path(contextPath + API_PATH + "/{id}").buildAndExpand(hash).toUri());
    return new ResponseEntity<>(headers, HttpStatus.CREATED);
  }

  @PostMapping(
      value = API_PATH + "/notifications",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<TransferBlockNotification> receiveTransferBlockNotification(
      @RequestBody TransferBlockNotification transferBlockNotification,
      UriComponentsBuilder builder)
      throws IOException, URISyntaxException, ParseException, NoSuchAlgorithmException,
          JOSEException {
    return transferBlockService
        .fetchTransferBlockByNotification(transferBlockNotification)
        .map(
            transferBlockHash ->
                builder.path(contextPath + API_PATH + "/" + transferBlockHash).build().toUri())
        .map(
            transferBlockUri ->
                ResponseEntity.created(transferBlockUri)
                    .body(
                        TransferBlockNotification.builder()
                            .transferBlockURL(transferBlockUri.toString())
                            .build()))
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "TransferBlockUrl in notification is not a known party."));
  }
}
