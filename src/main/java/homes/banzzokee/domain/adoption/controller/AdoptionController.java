package homes.banzzokee.domain.adoption.controller;

import homes.banzzokee.domain.adoption.dto.AdoptionRegisterRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionResponse;
import homes.banzzokee.domain.adoption.dto.AdoptionSearchRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionSearchRequest.AgeRange;
import homes.banzzokee.domain.adoption.dto.AdoptionSearchResponse;
import homes.banzzokee.domain.adoption.dto.AdoptionStatusChangeRequest;
import homes.banzzokee.domain.adoption.dto.AdoptionUpdateRequest;
import homes.banzzokee.domain.adoption.service.AdoptionService;
import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.validator.annotation.FileDuplicateValid;
import homes.banzzokee.global.validator.annotation.ImageFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
  public AdoptionResponse getAdoption(@PathVariable long adoptionId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return adoptionService.getAdoption(adoptionId, userDetails);
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

  @GetMapping
  public Slice<AdoptionSearchResponse> getAdoptionList(
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "10") int size,
      @RequestParam(required = false, defaultValue = "desc") String direction,
      @Size(max = 3) @RequestParam(required = false) List<BreedType> breed,
      @RequestParam(required = false) DogSize dogSize,
      @RequestParam(required = false) Boolean neutering,
      @RequestParam(required = false) Boolean healthChecked,
      @RequestParam(required = false) DogGender gender,
      @Min(0) @Max(100) @RequestParam(required = false) Integer minAge,
      @Min(0) @Max(100) @RequestParam(required = false) Integer maxAge
  ) {
    PageRequest pageRequest = PageRequest.of(page, size,
        Sort.by(Direction.fromString(direction), "createdAt"));
    AgeRange ageRange = new AgeRange(minAge, maxAge);
    AdoptionSearchRequest request = AdoptionSearchRequest.of(breed, dogSize, neutering,
        healthChecked, gender, ageRange);
    return adoptionService.getAdoptionList(request, pageRequest, userId);
  }

}
