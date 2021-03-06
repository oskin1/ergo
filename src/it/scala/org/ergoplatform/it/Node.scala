package org.ergoplatform.it

import org.asynchttpclient._
import org.ergoplatform.it.api.{NetworkNodeApi, NodeApi}
import org.ergoplatform.settings.ErgoSettings
import org.slf4j.{LoggerFactory, Logger}

import scala.concurrent.duration.FiniteDuration


class Node(settings: ErgoSettings, val nodeInfo: NodeInfo, override val client: AsyncHttpClient)
  extends NodeApi with NetworkNodeApi {
// todo after addresses will added
//  val privateKey: String = config.getString("private-key")
//  val publicKey: String = config.getString("public-key")
//  val address: String = config.getString("address")
//  val accountSeed: String = config.getString("account-seed")

  override protected val log: Logger =
    LoggerFactory.getLogger(s"${getClass.getName}.${settings.scorexSettings.network.nodeName}")

  override val chainId: Char = 'I'
  override val nodeName: String = s"it-test-client-to-${nodeInfo.networkIpAddress}"
  override val restAddress: String = "localhost"
  override val networkAddress: String = "localhost"
  override val nodeRestPort: Int = nodeInfo.hostRestApiPort
  override val networkPort: Int = nodeInfo.hostNetworkPort
  override val blockDelay: FiniteDuration = settings.chainSettings.blockInterval

}
