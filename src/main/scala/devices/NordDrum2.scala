/**
  * Copyright Homeaway, Inc 2016-Present. All Rights Reserved.
  * No unauthorized use of this software.
  */
package devices
import algebra.Messages.ControlChange
import algebra.Random

import scala.util.Random.nextInt

object NordDrum2 {

  case class NordDrum2(c: Int, d: Int) extends ControlChange(c, d)

  private def cc(c: Int)(d: Int) = NordDrum2(c, d)

  type Nord = Int => NordDrum2

  val Level: Nord                = cc(7)
  val Pan: Nord                  = cc(10)
  val NoiseFilterFrequency: Nord = cc(14)
  val NoiseFilterType: Nord      = cc(15)
  val NoiseFilterEnvelope: Nord  = cc(16)
  val NoiseFilterResonance: Nord = cc(17)
  val NoiseAttackRate: Nord      = cc(18)
  val NoiseAtkMode: Nord         = cc(19)
  val NoiseDecayType: Nord       = cc(20)
  val NoiseDecay: Nord           = cc(21)
  val NoiseDecayLo: Nord         = cc(22)
  val DistAmount: Nord           = cc(23)
  val DistType: Nord             = cc(24)
  val EQFrequency: Nord          = cc(25)
  val EQGain: Nord               = cc(26)
  val EchoFeedback: Nord         = cc(27)
  val EchoAmount: Nord           = cc(28)
  val EchoBPMMSB: Nord           = cc(29)
  val ToneSpectra: Nord          = cc(30)
  val TonePitchMSB: Nord         = cc(31)
  val ToneWave: Nord             = cc(46)
  val ToneTimbreDecay: Nord      = cc(47)
  val TonePunch: Nord            = cc(48)
  val ToneDecayType: Nord        = cc(49)
  val ToneDecay: Nord            = cc(50)
  val ToneDecLo: Nord            = cc(51)
  val ToneTimbre: Nord           = cc(52)
  val ToneTimbEnvelope: Nord     = cc(53)
  val ToneBendAmount: Nord       = cc(54)
  val ToneBendTime: Nord         = cc(55)
  val ClickLevel: Nord           = cc(56)
  val ClickType: Nord            = cc(57)
  val MixBalance: Nord           = cc(58)
  val MuteGroup: Nord            = cc(59)
  val EchoBPMLSB: Nord           = cc(61)
  val TonePitchLSB: Nord         = cc(63)
  val ChannelFocus: Nord         = cc(70)

  val controls: List[Nord] = List(
    Level,
    Pan,
    NoiseFilterFrequency,
    NoiseFilterType,
    NoiseFilterEnvelope,
    NoiseFilterResonance,
    NoiseAttackRate,
    NoiseAtkMode,
    NoiseDecayType,
    NoiseDecay,
    NoiseDecayLo,
    DistAmount,
    DistType,
    EQFrequency,
    EQGain,
    EchoFeedback,
    EchoAmount,
    EchoBPMMSB,
    ToneSpectra,
    TonePitchMSB,
    ToneWave,
    ToneTimbreDecay,
    TonePunch,
    ToneDecayType,
    ToneDecay,
    ToneDecLo,
    ToneTimbre,
    ToneTimbEnvelope,
    ToneBendAmount,
    ToneBendTime,
    ClickLevel,
    ClickType,
    MixBalance,
    MuteGroup,
    EchoBPMLSB,
    TonePitchLSB,
    ChannelFocus
  )

  implicit val randomChange: Random[NordDrum2] = () =>
    controls(nextInt(controls.size))(nextInt(Byte.MaxValue))
}
