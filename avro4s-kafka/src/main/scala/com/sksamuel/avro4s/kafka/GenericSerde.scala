package com.sksamuel.avro4s.kafka

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.sksamuel.avro4s.internal.{AvroSchema, SchemaFor}
import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream}
import org.apache.avro.Schema
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}

class GenericSerde[T >: Null : SchemaFor] extends Serde[T]
  with Deserializer[T]
  with Serializer[T]
  with Serializable {

  val schema: Schema = AvroSchema[T]

  override def serializer(): Serializer[T] = this

  override def deserializer(): Deserializer[T] = this

  override def deserialize(topic: String, data: Array[Byte]): T = {
    if (data == null) null else {
      val in = new ByteArrayInputStream(data)
      val input = AvroInputStream.binary[T](in, schema)
      input.iterator.next()
    }
  }

  override def close(): Unit = ()

  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = ()

  override def serialize(topic: String, data: T): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[T](baos, schema)
    output.write(data)
    output.close()
    baos.toByteArray
  }
}