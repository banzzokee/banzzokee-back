package homes.banzzokee.domain.shelter.controller;

import homes.banzzokee.domain.shelter.dto.ShelterRegisterRequest;
import homes.banzzokee.domain.shelter.service.ShelterService;
import homes.banzzokee.global.validator.annotation.ImageFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
      @ImageFile MultipartFile shelterImg, @RequestParam long userId) {
    // TODO: userId -> @AuthenticationPrincipal 바꾸기
    shelterService.registerShelter(request, shelterImg, userId);
  }

  @PostMapping("{shelterId}/verify")
  public void verifyShelter(@PathVariable long shelterId, @RequestParam long userId) {
    // TODO: ADMIN 사용자만 접근할 수 있도록 해야한다.
    // TODO: 위 TODO가 끝나면 userId 제거
    shelterService.verifyShelter(shelterId, userId);
  }
}
