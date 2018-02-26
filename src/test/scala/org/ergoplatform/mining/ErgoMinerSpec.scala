package org.ergoplatform.mining

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.TestKit
import org.ergoplatform.local.ErgoMiner.{MiningStatusRequest, MiningStatusResponse, StartMining}
import org.ergoplatform.local.TransactionGenerator.StartGeneration
import org.ergoplatform.local.{ErgoMiner, TransactionGenerator}
import org.ergoplatform.nodeView.state.StateType
import org.ergoplatform.nodeView.{ErgoNodeViewRef, ErgoReadersHolder}
import org.ergoplatform.settings.{ErgoSettings, TestingSettings}
import org.ergoplatform.utils.ErgoGenerators
import org.scalatest.{FlatSpecLike, Matchers}
import scorex.core.settings.ScorexSettings
import scorex.core.utils.NetworkTimeProvider

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}

class ErgoMinerSpec extends TestKit(ActorSystem()) with FlatSpecLike with Matchers with ErgoGenerators {

  val defaultAwaitDuration = 5 seconds
  implicit val timeout = akka.util.Timeout(defaultAwaitDuration)

  it should "not hang wwhile generating candidate block with large amount of txs" in {
    val defaultSettings: ErgoSettings = ErgoSettings.read(None)
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val nodeSettings = defaultSettings.nodeSettings.copy(mining = true, stateType = StateType.Utxo, offlineGeneration = true)
    val ergoSettings = defaultSettings.copy(nodeSettings = nodeSettings)
    val settings: ScorexSettings = ergoSettings.scorexSettings
    val timeProvider = new NetworkTimeProvider(settings.ntp)

    val nodeId = Array.fill(10)(1: Byte)

    val nodeViewHolderRef: ActorRef = ErgoNodeViewRef(ergoSettings, timeProvider)
    val readersHolderRef: ActorRef = ErgoReadersHolder(nodeViewHolderRef)
    val minerRef: ActorRef = ErgoMiner(ergoSettings, nodeViewHolderRef, readersHolderRef, nodeId, timeProvider)

    val testingSettings = TestingSettings(true, 1000)
    val txGen = TransactionGenerator(nodeViewHolderRef, testingSettings)
    txGen ! StartGeneration
    minerRef ! StartMining

    expectNoMessage(20 seconds)

    val respF = (minerRef ? MiningStatusRequest).mapTo[MiningStatusResponse]
    val resp = Await.result(respF, defaultAwaitDuration)

    //check that miner are online and feeling good
    resp.isMining shouldEqual true
    resp.candidateBlock shouldBe defined
  }
}
