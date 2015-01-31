package retrofit.converter;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 * A {@link Converter} which uses SimpleXML for reading and writing entities.
 *
 * @author Fabien Ric (fabien.ric@gmail.com)
 */
public class SimpleXMLConverter implements Converter {
  private static final boolean DEFAULT_STRICT = true;
  private static final String CHARSET = "UTF-8";
  private static final MediaType MEDIA_TYPE =
      MediaType.parse("application/xml; charset=" + CHARSET);

  private final Serializer serializer;

  private final boolean strict;

  public SimpleXMLConverter() {
    this(DEFAULT_STRICT);
  }

  public SimpleXMLConverter(boolean strict) {
    this(new Persister(), strict);
  }

  public SimpleXMLConverter(Serializer serializer) {
    this(serializer, DEFAULT_STRICT);
  }

  public SimpleXMLConverter(Serializer serializer, boolean strict) {
    this.serializer = serializer;
    this.strict = strict;
  }

  @Override public Object fromBody(ResponseBody body, Type type) throws IOException {
    InputStream is = body.byteStream();
    try {
      return serializer.read((Class<?>) type, is, strict);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        is.close();
      } catch (IOException ignored) {
      }
    }
  }

  @Override public RequestBody toBody(Object source, Type type) {
    byte[] bytes;
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      OutputStreamWriter osw = new OutputStreamWriter(bos, CHARSET);
      serializer.write(source, osw);
      osw.flush();
      bytes = bos.toByteArray();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
    return RequestBody.create(MEDIA_TYPE, bytes);
  }

  public boolean isStrict() {
    return strict;
  }
}
