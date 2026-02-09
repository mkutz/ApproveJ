package org.approvej.yaml.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import java.util.List;
import org.approvej.print.PropertyOrdering;

/**
 * A {@link BeanSerializerModifier} that ensures deterministic property ordering.
 *
 * <p>Field-backed properties are serialized first in their declaration order, followed by any
 * additional properties (e.g. from getters) in alphabetical order.
 */
final class DeterministicPropertyOrder extends BeanSerializerModifier {

  @Override
  public List<BeanPropertyWriter> orderProperties(
      SerializationConfig config,
      BeanDescription beanDesc,
      List<BeanPropertyWriter> beanProperties) {
    return PropertyOrdering.reorder(
        beanDesc.getBeanClass(), beanProperties, BeanPropertyWriter::getName);
  }
}
