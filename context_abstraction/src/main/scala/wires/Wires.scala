package wires

import ujson.*
import scala.util.{Try, Success, Failure}

/** There was an error during the parsing of a JSON message. */
case class DecodingException(msg: String)
    extends Exception(f"Error while trying to decode JSON message: $msg.")

/** Encodes an object of type [[T]] to a [[ujson.Value]] */
trait Encoder[T]:
  def encode(t: T): ujson.Value

/** Decodes an object of type [[T]] from a [[ujson.Value]] */
trait Decoder[T]:
  def decode(json: ujson.Value): util.Try[T]

/** Provides a way to decode and encode an object of type [[T]] to [[Value]] */
trait WireFormat[T] extends Encoder[T] with Decoder[T]

def encodeWire[T](t: T)(using wt: WireFormat[T]): ujson.Value =
  wt.encode(t)

def decodeWire[T](js: ujson.Value)(using wt: WireFormat[T]): Try[T] =
  wt.decode(js)

object IdentityWire extends WireFormat[ujson.Value]:
  def encode(t: ujson.Value): Value = t
  def decode(js: Value): Try[ujson.Value] = Success(js)

object OldBooleanWire extends WireFormat[Boolean]:
  def encode(t: Boolean): Value = Bool(t)
  def decode(js: Value): Try[Boolean] = Try(js.bool)

given WireFormat[Boolean] with
  def encode(t: Boolean): Value = Bool(t)
  def decode(js: Value): Try[Boolean] = Try(js.bool)

object StringWire extends WireFormat[String]:
  def encode(t: String): Value = Str(t)
  def decode(js: Value): Try[String] = Try(js.str)

given WireFormat[String] with
  def encode(t: String): Value = Str(t)
  def decode(json: Value): Try[String] = Try(json.str)

object IntWire extends WireFormat[Int]:
  def encode(t: Int): Value = Num(t)
  def decode(js: Value): Try[Int] = Try(js.num.toInt)

given WireFormat[Int] with
  def encode(t: Int): Value = Num(t)
  def decode(js: Value): Try[Int] = Try(js.num.toInt)

case class OptionWire[T](wt: WireFormat[T]) extends WireFormat[Option[T]]:
  def encode(o: Option[T]): ujson.Value =
    o match
      case None    => Obj()
      case Some(t) => Obj("get" -> wt.encode(t))
  def decode(js: ujson.Value): Try[Option[T]] = Try:
    js.obj.get("get").map(wt.decode(_).get)

given [T: WireFormat]: WireFormat[Option[T]] with
  def encode(o: Option[T]): ujson.Value =
    o match
      case None    => Obj()
      case Some(t) => Obj("get" -> encodeWire(t))

    def decode(js: ujson.Value): Try[Option[T]] = Try:
      js.obj.get("get").map(decodeWire(_).get)

case class TryWire[T](wt: WireFormat[T]) extends WireFormat[Try[T]]:
  def encode(t: Try[T]): Value =
    t match
      case Failure(exn) =>
        Obj(
          "type" -> "failure",
          "msg" -> Str(exn.getMessage),
          "stacktrace" -> exn.getStackTrace.mkString("StackTrace(", ", ", ")")
        )
      case Success(t) =>
        Obj("type" -> "success", "get" -> wt.encode(t))

  def decode(json: Value): Try[Try[T]] = Try:
    json("type").str match
      case "failure" =>
        Failure(Exception(json("msg").str))
      case "success" =>
        Success(wt.decode(json("get")).get)
      case _ =>
        throw DecodingException(f"Unexpected try: $json")

given [T: WireFormat]: WireFormat[Try[T]] with
  def encode(t: Try[T]): Value =
    t match
      case Failure(exn) =>
        Obj(
          "type" -> "failure",
          "msg" -> Str(exn.getMessage()),
          "stacktrace" -> exn.getStackTrace.mkString("StackTrace(", ", ", ")")
        )
      case Success(t) =>
        Obj("type" -> "success", "get" -> encodeWire(t))

  def decode(json: Value): Try[Try[T]] = Try:
    json("type").str match
      case "failure" =>
        Failure(Exception(json("msg").str))
      case "success" =>
        Success(decodeWire(json("get")).get)
      case _ =>
        throw DecodingException(f"Unexpected try: $json")

case class SeqWire[T](wt: WireFormat[T]) extends WireFormat[Seq[T]]:
  def encode(s: Seq[T]): ujson.Value =
    Arr(s.map(wt.encode)*)
  def decode(js: ujson.Value): Try[Seq[T]] = Try:
    js.arr.map(wt.decode(_).get).toSeq

given [T: WireFormat]: WireFormat[Seq[T]] with
  def encode(s: Seq[T]): ujson.Value =
    Arr(s.map(encodeWire)*)
  def decode(js: ujson.Value): Try[Seq[T]] = Try:
    js.arr.map(decodeWire(_).get).toSeq

case class CastWire[T1, T2: WireFormat](enc: T1 => T2, dec: T2 => Try[T1])
    extends WireFormat[T1]:
  def encode(t1: T1): ujson.Value =
    encodeWire(enc(t1))
  def decode(js: ujson.Value): Try[T1] =
    decodeWire(js).flatMap(dec)

given [T: WireFormat]: WireFormat[Set[T]] =
  CastWire[Set[T], Seq[T]](
    (s: Set[T]) => s.toSeq,
    (s: Seq[T]) => Success(s.toSet)
  )
given [T1: WireFormat, T2: WireFormat]: WireFormat[(T1, T2)] with
  def encode(p: (T1, T2)): ujson.Value =
    Arr(encodeWire(p._1), encodeWire(p._2))
  def decode(js: ujson.Value): Try[(T1, T2)] = Try:
    (decodeWire(js.arr(0)).get, decodeWire(js.arr(1)).get)

given [K: WireFormat, V: WireFormat]: WireFormat[Map[K, V]] =
  CastWire[Map[K, V], Seq[(K, V)]](
    (m: Map[K, V]) => m.toSeq,
    (s: Seq[(K, V)]) => Success(s.toMap)
  )