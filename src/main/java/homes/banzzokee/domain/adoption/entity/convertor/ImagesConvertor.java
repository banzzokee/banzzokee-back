package homes.banzzokee.domain.adoption.entity.convertor;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import homes.banzzokee.domain.type.S3Object;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Converter
public class ImagesConvertor implements AttributeConverter<List<S3Object>, String> {

  private final Gson gson = new Gson();

  @Override
  public String convertToDatabaseColumn(List<S3Object> attribute) {

    return gson.toJson(attribute);
  }

  @Override
  public List<S3Object> convertToEntityAttribute(String dbData) {

    Type listOfS3Object = new TypeToken<ArrayList<S3Object>>() {}.getType();

    return gson.fromJson(dbData, listOfS3Object);
  }
}
