package homes.banzzokee.domain.type;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AdoptionStatus {

  ADOPTING("분양중"),
  RESERVING("예약중"),
  FINISHED("분양완료")
  ;

  private final String status;
}
