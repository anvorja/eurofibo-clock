package borja.eurofiboclock.domain.algorithm

import borja.eurofiboclock.domain.model.{CapitalSelection, EuropeanJourney, TemporalPassport}

/** The result of running a strategy.
  *
  * A1, A2 and A3 answer different business questions and therefore return
  * different shapes; this ADT keeps them under one type so the use case and the
  * REST adapter can treat "run any algorithm" uniformly.
  */
enum SelectionOutcome:
  case Selected(value: CapitalSelection)   // A1
  case Passport(value: TemporalPassport)   // A2
  case Journey(value: EuropeanJourney)     // A3
