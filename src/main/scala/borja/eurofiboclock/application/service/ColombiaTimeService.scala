package borja.eurofiboclock.application.service

import cats.Functor
import cats.syntax.functor.*

import borja.eurofiboclock.domain.model.ColombiaTime
import borja.eurofiboclock.domain.port.in.GetColombiaTimeUseCase
import borja.eurofiboclock.domain.port.out.ClockPort

/** Application service implementing [[GetColombiaTimeUseCase]].
  *
  * It only orchestrates: ask the clock for an instant, project it onto the
  * Colombia zone. All the "what does Colombia time mean" knowledge lives in the
  * [[ColombiaTime]] domain object.
  */
final class ColombiaTimeService[F[_]: Functor](clock: ClockPort[F])
    extends GetColombiaTimeUseCase[F]:

  def currentTime: F[ColombiaTime] =
    clock.now.map(instant => ColombiaTime.fromZoned(instant.atZone(ColombiaTime.Zone)))
