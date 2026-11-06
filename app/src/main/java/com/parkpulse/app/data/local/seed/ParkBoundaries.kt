package com.parkpulse.app.data.local.seed

import com.parkpulse.app.domain.model.LatLng

/**
 * Park boundary polygons extracted from the user's Overpass Turbo GeoJSON export.
 * Source: OpenStreetMap contributors; OSM object IDs are documented per polygon.
 * GeoJSON coordinates are [longitude, latitude]; this file converts them to LatLng(latitude, longitude).
 * Only confidently name-matched features are populated. Unmatched parks remain null.
 */
object ParkBoundaries {

    // Sambhaji Udyan — way/199261924
    val sambhajiPark: List<LatLng> = listOf(
        LatLng(18.5204036, 73.8462967),
        LatLng(18.5202898, 73.8464352),
        LatLng(18.5201766, 73.8463838),
        LatLng(18.5201373, 73.8464356),
        LatLng(18.5200291, 73.8463455),
        LatLng(18.5200677, 73.8462931),
        LatLng(18.5198959, 73.8460936),
        LatLng(18.5197033, 73.8463521),
        LatLng(18.5190467, 73.8459043),
        LatLng(18.5189285, 73.8458859),
        LatLng(18.5188541, 73.8459228),
        LatLng(18.5188060, 73.8459921),
        LatLng(18.5188585, 73.8460890),
        LatLng(18.5197208, 73.8467953),
        LatLng(18.5206656, 73.8478434),
        LatLng(18.5210690, 73.8483648),
        LatLng(18.5222683, 73.8480555),
        LatLng(18.5223165, 73.8479309),
        LatLng(18.5204036, 73.8462967)
    )

    // Sarasbaug — way/53272975
    val sarasBaug: List<LatLng> = listOf(
        LatLng(18.5023015, 73.8522143),
        LatLng(18.5016500, 73.8522127),
        LatLng(18.4995577, 73.8517935),
        LatLng(18.4994864, 73.8517869),
        LatLng(18.4993573, 73.8521520),
        LatLng(18.4998199, 73.8536816),
        LatLng(18.5000481, 73.8538208),
        LatLng(18.5009416, 73.8538315),
        LatLng(18.5021116, 73.8537466),
        LatLng(18.5023015, 73.8522143)
    )

    // P L Deshpande Garden — way/197954185
    val puLaDeshpandeUdyan: List<LatLng> = listOf(
        LatLng(18.4931838, 73.8354038),
        LatLng(18.4924811, 73.8360332),
        LatLng(18.4921510, 73.8356939),
        LatLng(18.4901784, 73.8375641),
        LatLng(18.4901784, 73.8378409),
        LatLng(18.4904747, 73.8380730),
        LatLng(18.4923572, 73.8386038),
        LatLng(18.4928979, 73.8375851),
        LatLng(18.4933024, 73.8377488),
        LatLng(18.4934513, 73.8382457),
        LatLng(18.4939993, 73.8384829),
        LatLng(18.4941952, 73.8382557),
        LatLng(18.4938671, 73.8379928),
        LatLng(18.4945233, 73.8371479),
        LatLng(18.4950702, 73.8376281),
        LatLng(18.4951734, 73.8374659),
        LatLng(18.4931838, 73.8354038)
    )

    // Empress Botanical Garden — way/53154871
    val empressGarden: List<LatLng> = listOf(
        LatLng(18.5152230, 73.8945834),
        LatLng(18.5133306, 73.8956444),
        LatLng(18.5125388, 73.8963003),
        LatLng(18.5119608, 73.8968103),
        LatLng(18.5116827, 73.8969576),
        LatLng(18.5109586, 73.8972534),
        LatLng(18.5110067, 73.8974228),
        LatLng(18.5110431, 73.8976507),
        LatLng(18.5110496, 73.8980259),
        LatLng(18.5110498, 73.8993464),
        LatLng(18.5110139, 73.9001021),
        LatLng(18.5118147, 73.9001227),
        LatLng(18.5128047, 73.9003482),
        LatLng(18.5133093, 73.9002713),
        LatLng(18.5135204, 73.9000937),
        LatLng(18.5138719, 73.8998392),
        LatLng(18.5140581, 73.8996726),
        LatLng(18.5142542, 73.8995326),
        LatLng(18.5144322, 73.8993749),
        LatLng(18.5144651, 73.8992563),
        LatLng(18.5143193, 73.8991474),
        LatLng(18.5134497, 73.8990790),
        LatLng(18.5134360, 73.8989905),
        LatLng(18.5134741, 73.8985539),
        LatLng(18.5134018, 73.8980735),
        LatLng(18.5136968, 73.8979555),
        LatLng(18.5139003, 73.8978268),
        LatLng(18.5141037, 73.8976444),
        LatLng(18.5141444, 73.8973332),
        LatLng(18.5141189, 73.8965728),
        LatLng(18.5141909, 73.8963426),
        LatLng(18.5147107, 73.8964886),
        LatLng(18.5147040, 73.8968504),
        LatLng(18.5149583, 73.8965930),
        LatLng(18.5151923, 73.8965071),
        LatLng(18.5154047, 73.8964411),
        LatLng(18.5153398, 73.8961723),
        LatLng(18.5152832, 73.8957821),
        LatLng(18.5152230, 73.8945834)
    )

    // Bund Garden — way/532470092
    val bundGarden: List<LatLng> = listOf(
        LatLng(18.5419412, 73.8832980),
        LatLng(18.5410969, 73.8833758),
        LatLng(18.5412673, 73.8838921),
        LatLng(18.5412880, 73.8839243),
        LatLng(18.5413175, 73.8839458),
        LatLng(18.5413477, 73.8839602),
        LatLng(18.5413786, 73.8839636),
        LatLng(18.5414135, 73.8839595),
        LatLng(18.5414456, 73.8839552),
        LatLng(18.5418255, 73.8838948),
        LatLng(18.5418547, 73.8841255),
        LatLng(18.5414479, 73.8841952),
        LatLng(18.5413983, 73.8842086),
        LatLng(18.5413608, 73.8842335),
        LatLng(18.5413302, 73.8842663),
        LatLng(18.5413112, 73.8843032),
        LatLng(18.5412997, 73.8843421),
        LatLng(18.5412997, 73.8843830),
        LatLng(18.5413042, 73.8844232),
        LatLng(18.5414008, 73.8849181),
        LatLng(18.5414402, 73.8850401),
        LatLng(18.5414822, 73.8851072),
        LatLng(18.5415432, 73.8851742),
        LatLng(18.5415979, 73.8851997),
        LatLng(18.5416475, 73.8851984),
        LatLng(18.5416907, 73.8851917),
        LatLng(18.5421497, 73.8851099),
        LatLng(18.5419412, 73.8832980)
    )

    // Peshve Udyan — way/197954183
    val peshwePark: List<LatLng> = listOf(
        LatLng(18.4995577, 73.8517935),
        LatLng(18.5016500, 73.8522127),
        LatLng(18.5023015, 73.8522143),
        LatLng(18.5027754, 73.8521685),
        LatLng(18.5031226, 73.8508808),
        LatLng(18.5013148, 73.8508555),
        LatLng(18.5010274, 73.8509565),
        LatLng(18.4997823, 73.8509881),
        LatLng(18.4995577, 73.8517935)
    )

    // Kamala Nehru Park — way/1030015753
    val kamlaNehruPark: List<LatLng> = listOf(
        LatLng(18.5175685, 73.8341298),
        LatLng(18.5165079, 73.8340312),
        LatLng(18.5164232, 73.8347460),
        LatLng(18.5164057, 73.8354947),
        LatLng(18.5169520, 73.8355964),
        LatLng(18.5174399, 73.8355964),
        LatLng(18.5175685, 73.8341298)
    )

    // Osho Teerth Park — way/28217451
    val oshoTeerthPark: List<LatLng> = listOf(
        LatLng(18.5332794, 73.8908723),
        LatLng(18.5333008, 73.8910777),
        LatLng(18.5336390, 73.8911201),
        LatLng(18.5338575, 73.8909554),
        LatLng(18.5340888, 73.8909851),
        LatLng(18.5345423, 73.8910857),
        LatLng(18.5351364, 73.8910342),
        LatLng(18.5355758, 73.8910342),
        LatLng(18.5361373, 73.8910342),
        LatLng(18.5364466, 73.8910342),
        LatLng(18.5368209, 73.8911630),
        LatLng(18.5371708, 73.8914720),
        LatLng(18.5374203, 73.8916193),
        LatLng(18.5375452, 73.8918067),
        LatLng(18.5380172, 73.8920299),
        LatLng(18.5385298, 73.8922015),
        LatLng(18.5391320, 73.8924161),
        LatLng(18.5395364, 73.8925647),
        LatLng(18.5395695, 73.8917048),
        LatLng(18.5388553, 73.8915235),
        LatLng(18.5382857, 73.8914033),
        LatLng(18.5376835, 73.8911201),
        LatLng(18.5370976, 73.8907338),
        LatLng(18.5362350, 73.8904849),
        LatLng(18.5355351, 73.8904334),
        LatLng(18.5350387, 73.8903733),
        LatLng(18.5342982, 73.8903133),
        LatLng(18.5332871, 73.8902859),
        LatLng(18.5332794, 73.8908723)
    )

    // Kalyani Nagar Joggers Park — way/218691432
    val joggersPark: List<LatLng> = listOf(
        LatLng(18.5499442, 73.9024012),
        LatLng(18.5497201, 73.9023684),
        LatLng(18.5494296, 73.9033253),
        LatLng(18.5508827, 73.9035876),
        LatLng(18.5509588, 73.9024152),
        LatLng(18.5499841, 73.9022158),
        LatLng(18.5499442, 73.9024012)
    )

    // Pashan Lake Park — way/395898751
    val pashanLake: List<LatLng> = listOf(
        LatLng(18.5369946, 73.7894253),
        LatLng(18.5370659, 73.7892596),
        LatLng(18.5370969, 73.7891065),
        LatLng(18.5370897, 73.7890790),
        LatLng(18.5370897, 73.7890364),
        LatLng(18.5370498, 73.7890055),
        LatLng(18.5370251, 73.7889754),
        LatLng(18.5370521, 73.7887346),
        LatLng(18.5370434, 73.7885811),
        LatLng(18.5370395, 73.7885110),
        LatLng(18.5370424, 73.7883189),
        LatLng(18.5369075, 73.7882721),
        LatLng(18.5367016, 73.7881636),
        LatLng(18.5361994, 73.7879332),
        LatLng(18.5360972, 73.7877929),
        LatLng(18.5359411, 73.7879860),
        LatLng(18.5358697, 73.7880811),
        LatLng(18.5360158, 73.7882737),
        LatLng(18.5360811, 73.7883578),
        LatLng(18.5361232, 73.7884138),
        LatLng(18.5361896, 73.7885131),
        LatLng(18.5362605, 73.7886263),
        LatLng(18.5363757, 73.7888090),
        LatLng(18.5364726, 73.7889238),
        LatLng(18.5365412, 73.7889935),
        LatLng(18.5365997, 73.7890526),
        LatLng(18.5366633, 73.7891196),
        LatLng(18.5367472, 73.7892135),
        LatLng(18.5368283, 73.7892875),
        LatLng(18.5369946, 73.7894253)
    )

    // Katraj Lake — way/54937943
    val katrajLake: List<LatLng> = listOf(
        LatLng(18.4568426, 73.8617870),
        LatLng(18.4567996, 73.8615793),
        LatLng(18.4567163, 73.8614362),
        LatLng(18.4565303, 73.8613897),
        LatLng(18.4561242, 73.8612971),
        LatLng(18.4559522, 73.8612504),
        LatLng(18.4557544, 73.8611854),
        LatLng(18.4556001, 73.8611237),
        LatLng(18.4554133, 73.8610347),
        LatLng(18.4551094, 73.8608684),
        LatLng(18.4545180, 73.8605156),
        LatLng(18.4537174, 73.8603661),
        LatLng(18.4536061, 73.8603063),
        LatLng(18.4534883, 73.8601614),
        LatLng(18.4534185, 73.8601154),
        LatLng(18.4533160, 73.8600970),
        LatLng(18.4531851, 73.8601338),
        LatLng(18.4530306, 73.8600955),
        LatLng(18.4527403, 73.8600289),
        LatLng(18.4524093, 73.8599595),
        LatLng(18.4522443, 73.8599296),
        LatLng(18.4519726, 73.8598870),
        LatLng(18.4518800, 73.8599195),
        LatLng(18.4517196, 73.8601472),
        LatLng(18.4516271, 73.8601829),
        LatLng(18.4515407, 73.8601374),
        LatLng(18.4514296, 73.8600333),
        LatLng(18.4513802, 73.8599000),
        LatLng(18.4513587, 73.8597016),
        LatLng(18.4511921, 73.8599683),
        LatLng(18.4511859, 73.8602057),
        LatLng(18.4511396, 73.8603748),
        LatLng(18.4510316, 73.8604691),
        LatLng(18.4509915, 73.8605472),
        LatLng(18.4509699, 73.8606870),
        LatLng(18.4509206, 73.8608171),
        LatLng(18.4508064, 73.8608854),
        LatLng(18.4505872, 73.8608804),
        LatLng(18.4504565, 73.8609772),
        LatLng(18.4503917, 73.8610715),
        LatLng(18.4503577, 73.8612374),
        LatLng(18.4503824, 73.8613545),
        LatLng(18.4511224, 73.8614005),
        LatLng(18.4522859, 73.8615041),
        LatLng(18.4525142, 73.8615822),
        LatLng(18.4527006, 73.8617360),
        LatLng(18.4528539, 73.8618687),
        LatLng(18.4529540, 73.8619620),
        LatLng(18.4530908, 73.8620994),
        LatLng(18.4533323, 73.8623568),
        LatLng(18.4534490, 73.8624863),
        LatLng(18.4535971, 73.8625513),
        LatLng(18.4537938, 73.8625661),
        LatLng(18.4539957, 73.8625848),
        LatLng(18.4542135, 73.8626094),
        LatLng(18.4543571, 73.8626292),
        LatLng(18.4544825, 73.8626489),
        LatLng(18.4547441, 73.8629493),
        LatLng(18.4549083, 73.8631498),
        LatLng(18.4552476, 73.8632831),
        LatLng(18.4553371, 73.8632929),
        LatLng(18.4554882, 73.8634652),
        LatLng(18.4555839, 73.8635140),
        LatLng(18.4557320, 73.8635465),
        LatLng(18.4559633, 73.8634782),
        LatLng(18.4563119, 73.8630294),
        LatLng(18.4564822, 73.8628993),
        LatLng(18.4568426, 73.8617870)
    )

    // No confidently matching polygon found in this export for Khadakwasla Lakefront.
    val khadakwaslaLakefront: List<LatLng>? = null

    // No confidently matching polygon found in this export for Vetal Tekdi.
    val vetalTekdi: List<LatLng>? = null

    // No confidently matching polygon found in this export for Taljai Tekdi.
    val taljaiTekdi: List<LatLng>? = null

    // No confidently matching polygon found in this export for ARAI Hill.
    val araiHill: List<LatLng>? = null

    // No confidently matching polygon found in this export for Pashan-Sus Biodiversity Park.
    val pashanSusPark: List<LatLng>? = null

    // No confidently matching polygon found in this export for Bhamburda Van Udyan.
    val bhamburdaVanUdyan: List<LatLng>? = null

    // No confidently matching polygon found in this export for Parvati Hill.
    val parvatiHill: List<LatLng>? = null

    // No confidently matching polygon found in this export for Baner Hill.
    val banerHill: List<LatLng>? = null

    // No confidently matching polygon found in this export for Rajiv Gandhi Udyan (Katraj).
    val rajivGandhiUdyan: List<LatLng>? = null

    // No confidently matching polygon found in this export for Shaniwar Wada Lawns.
    val shaniwarWadaLawns: List<LatLng>? = null

}