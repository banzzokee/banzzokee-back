package homes.banzzokee.domain.test.controller;

import static homes.banzzokee.global.error.ErrorCode.FAILED;

import homes.banzzokee.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * test controller
 * 추후에 삭제
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tests")
public class TestController {

  @GetMapping("/{testId}")
  public ResponseEntity<String> getStringTest(@PathVariable Long testId) {

    if (testId == 1) {
      throw new CustomException(FAILED);
    }

    return ResponseEntity.ok("테스트 입니다.");
  }
}