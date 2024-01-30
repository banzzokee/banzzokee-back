package homes.banzzokee.domain.adoption.controller;

import homes.banzzokee.domain.adoption.dto.AdoptionRegisterRequest;
import homes.banzzokee.domain.adoption.service.AdoptionService;
import homes.banzzokee.global.validator.annotation.ImageFile;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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
      List<@ImageFile MultipartFile> multipartFiles // Todo @AuthenticationPrincipal
      ) {
    adoptionService.registerAdoption(request, multipartFiles, 1L);
  }

}
