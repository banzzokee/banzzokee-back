package homes.banzzokee.domain.adoption.controller;

import homes.banzzokee.domain.adoption.dto.AdoptionRegisterRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionResponse;
import homes.banzzokee.domain.adoption.dto.AdoptionStatusChangeRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionUpdateRequest;
import homes.banzzokee.domain.adoption.service.AdoptionService;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.validator.annotation.FileDuplicateValid;
import homes.banzzokee.global.validator.annotation.ImageFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/adoptions")
public class AdoptionController {

  private final AdoptionService adoptionService;

  @PostMapping
  public void registerAdoption(@Valid @RequestPart AdoptionRegisterRequest request,
      @Size(min = 1, max = 8) @FileDuplicateValid @ImageFile List<MultipartFile> images,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    adoptionService.registerAdoption(request, images, userDetails.getUserId());
  }

  @GetMapping("/{adoptionId}")
  public AdoptionResponse getAdoption(@PathVariable long adoptionId) {
    return adoptionService.getAdoption(adoptionId);
  }

  @PutMapping("/{adoptionId}")
  public void updateAdoption(@PathVariable long adoptionId,
      @Valid @RequestPart AdoptionUpdateRequest request,
      @Size(min = 1, max = 8) @FileDuplicateValid @ImageFile List<MultipartFile> images,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    adoptionService.updateAdoption(adoptionId, request, images,
        userDetails.getUserId());
  }

  @PatchMapping("/{adoptionId}/status")
  public void changeAdoptionStatus(@PathVariable long adoptionId,
      @Valid @RequestBody AdoptionStatusChangeRequest request,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    adoptionService.changeAdoptionStatus(adoptionId, request,
        userDetails.getUserId());
  }

  @DeleteMapping("/{adoptionId}")
  public void deleteAdoption(@PathVariable long adoptionId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    adoptionService.deleteAdoption(adoptionId, userDetails.getUserId());
  }

}
