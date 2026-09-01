package borja.eurofiboclock.domain.model

import java.time.Instant

/** The Colombia wall-clock time reduced to the three numbers the selection
  * algorithms actually use: `hours`, `minutes`, `seconds`.
  */
final case class WallClock(hours: Int, minutes: Int, seconds: Int):

  /** `"13:40:34"` — the shape `getColombianTime()` returns in the spec. */
  def label: String = f"$hours%02d:$minutes%02d:$seconds%02d"

  /** `"134034"` — used to build the A2 passport code. */
  def compact: String = f"$hours%02d$minutes%02d$seconds%02d"

object WallClock:

  def fromInstant(instant: Instant): WallClock =
    val t = instant.atZone(ColombiaTime.Zone).toLocalTime
    WallClock(t.getHour, t.getMinute, t.getSecond)
