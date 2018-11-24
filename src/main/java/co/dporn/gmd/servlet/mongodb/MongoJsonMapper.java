package co.dporn.gmd.servlet.mongodb;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MongoJsonMapper {
	protected MongoJsonMapper() {
	}
	private static ObjectMapper _mapper;
	public static ObjectMapper get() {
		if (_mapper!=null) {
			return _mapper;
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		mapper.enable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
		mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
		mapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, true);
		mapper.configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
		mapper.configure(Feature.IGNORE_UNKNOWN, true);
		return _mapper=mapper;
	}
}
