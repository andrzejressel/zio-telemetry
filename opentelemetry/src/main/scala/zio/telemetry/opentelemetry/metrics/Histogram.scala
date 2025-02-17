package zio.telemetry.opentelemetry.metrics

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.DoubleHistogram
import io.opentelemetry.context.Context
import zio._
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.metrics.internal.{Instrument, logAnnotatedAttributes}

/**
 * A Histogram instrument that records values of type `A`
 *
 * @tparam A
 *   according to the specification, it can be either [[scala.Long]] or [[scala.Double]] type
 */
trait Histogram[-A] extends Instrument[A] {

  /**
   * Records a value.
   *
   * It uses the context taken from the [[zio.telemetry.opentelemetry.context.ContextStorage]] to associate with this
   * measurement.
   *
   * @param value
   *   increment amount. MUST be non-negative
   * @param attributes
   *   set of attributes to associate with the value
   */
  def record(value: A, attributes: Attributes = Attributes.empty)(implicit trace: Trace): UIO[Unit]

}

object Histogram {

  private[metrics] def double(
    histogram: DoubleHistogram,
    ctxStorage: ContextStorage,
    logAnnotated: Boolean
  ): Histogram[Double] =
    new Histogram[Double] {

      override def record0(value: Double, attributes: Attributes = Attributes.empty, context: Context): Unit =
        histogram.record(value, attributes, context)

      override def record(value: Double, attributes: Attributes = Attributes.empty)(implicit trace: Trace): UIO[Unit] =
        for {
          annotated <- logAnnotatedAttributes(attributes, logAnnotated)
          ctx       <- ctxStorage.get
        } yield record0(value, annotated, ctx)

    }

}
