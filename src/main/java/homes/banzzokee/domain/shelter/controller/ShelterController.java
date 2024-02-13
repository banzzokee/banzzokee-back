package homes.banzzokee.domain.shelter.controller;

import homes.banzzokee.domain.shelter.dto.ShelterRegisterRequest;
import homes.banzzokee.domain.shelter.dto.ShelterUpdateRequest;
import homes.banzzokee.domain.shelter.dto.ShelterUpdateResponse;
import homes.banzzokee.domain.shelter.service.ShelterService;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.validator.annotation.ImageFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shelters")
public class ShelterController {

  private final ShelterService shelterService;

  @PostMapping
  public void registerShelter(@Valid @RequestPart ShelterRegisterRequest request,
      @ImageFile MultipartFile shelterImg,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    shelterService.registerShelter(request, shelterImg, userDetails.getUserId());
  }

  @PostMapping("{shelterId}/verify")
  public void verifyShelter(@PathVariable long shelterId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    shelterService.verifyShelter(shelterId, userDetails.getUserId());
  }

  @PatchMapping("{shelterId}")
  public ShelterUpdateResponse updateShelter(@PathVariable long shelterId,
      @Valid @RequestPart ShelterUpdateRequest request,
      @ImageFile MultipartFile shelterImg,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return shelterService.updateShelter(shelterId, request, shelterImg,
        userDetails.getUserId());
  }

  @DeleteMapping("{shelterId}")
  public void unregisterShelter(@PathVariable long shelterId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    shelterService.unregisterShelter(shelterId, userDetails.getUserId());
  }
}
