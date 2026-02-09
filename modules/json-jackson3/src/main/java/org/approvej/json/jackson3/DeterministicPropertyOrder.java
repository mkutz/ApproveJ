package org.approvej.json.jackson3;

import java.util.List;
import org.approvej.print.PropertyOrdering;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

/**
 * A {@link ValueSerializerModifier} that ensures deterministic property ordering.
 *
 * <p>Field-backed properties are serialized first in their declaration order, followed by any
 * additional properties (e.g. from getters) in alphabetical order.
 */
final class DeterministicPropertyOrder extends ValueSerializerModifier {

  @Override
  public List<BeanPropertyWriter> orderProperties(
      SerializationConfig config,
      BeanDescription.Supplier beanDesc,
      List<BeanPropertyWriter> beanProperties) {
    return PropertyOrdering.reorder(
        beanDesc.getBeanClass(), beanProperties, BeanPropertyWriter::getName);
  }
}
