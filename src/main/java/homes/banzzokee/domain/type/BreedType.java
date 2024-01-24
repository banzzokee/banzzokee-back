package homes.banzzokee.domain.type;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BreedType {
  SIBERIAN_HUSKY("시베리안 허스키"),
  POODLE("푸들"),
  SHEPHERD("셰퍼드"),
  ALASKAN_MALAMUTE("알래스카 맬러뮤트"),
  DOBERMAN("도베르만"),
  RETRIEVER("리트리버"),
  BEDLINGTON_TERRIER("베들링턴 테리어"),
  GREYHOUND("그레이하운드"),
  WELSH_CORGI("웰시코기"),
  SAMOYED("사모예드"),
  SHIBA_INU("시바"),
  SPITZ("스피츠"),
  SCHNAUZER("슈나우저"),
  BICHON("비숑"),
  SHIHTZU("시츄"),
  JACK_RUSSELL_TERRIOR("잭 러셀 테리어"),
  POMERANIAN("포메라니안"),
  PINSCHER("핀셔"),
  PAPILON("파피용"),
  YORKSHIRE_TERRIER("요크셔 테리어"),
  MALTESE("말티즈"),
  DACHSHUND("닥스훈트"),
  CHIHUAHUA("치와와"),
  PUG("퍼그"),
  BULLDOG("불독"),
  MIX("믹스"),
  ETC("기타");

  private final String breed;

  private static final Map<String, BreedType> breedTypes = Collections.unmodifiableMap(
      Stream.of(values())
          .collect(Collectors.toMap(BreedType::getBreed, Function.identity()))
  );

  public static BreedType findByString(String breed) {
    return breedTypes.get(breed);
  }

  public static boolean contains(String breed) {
    return breedTypes.containsKey(breed);
  }
}
