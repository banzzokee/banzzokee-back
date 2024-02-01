package homes.banzzokee.domain.adoption.controller;

import homes.banzzokee.domain.adoption.dto.AdoptionRegisterRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionResponse;
import homes.banzzokee.domain.adoption.service.AdoptionService;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.validator.annotation.FileDuplicateValid;
import homes.banzzokee.global.validator.annotation.ImageFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
      @Size(min = 1, max = 8) @FileDuplicateValid @ImageFile List<MultipartFile> multipartFiles,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    adoptionService.registerAdoption(request, multipartFiles,
        userDetails.getUser().getId());
  }

  @GetMapping("/{adoptionId}")
  public AdoptionResponse getAdoption(@PathVariable long adoptionId) {
    return adoptionService.getAdoption(adoptionId);
  }

}
