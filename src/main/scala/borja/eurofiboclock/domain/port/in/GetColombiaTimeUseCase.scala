package borja.eurofiboclock.domain.port.in

import borja.eurofiboclock.domain.model.ColombiaTime

/** Driving port: "give me the current local time in Colombia". */
trait GetColombiaTimeUseCase[F[_]]:
  def currentTime: F[ColombiaTime]
