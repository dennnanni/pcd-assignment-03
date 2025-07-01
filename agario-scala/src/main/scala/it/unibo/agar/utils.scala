package it.unibo.agar

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import com.typesafe.config.ConfigFactory

val seeds = List(2551, 2552) // seed used in the configuration

def startup[X](file: String = "base-cluster", port: Int)(root: => Behavior[X]): ActorSystem[X] =
  // Override the configuration of the port
  val config = ConfigFactory
    .parseString(s"""akka.remote.artery.canonical.port=$port""")
    .withFallback(ConfigFactory.load(file))

  // Create an Akka system
  ActorSystem(root, file, config)

def startupWithSeeds[X](file: String = "base-cluster", port: Int, seeds: List[Int] = Nil)
              (root: => Behavior[X]): ActorSystem[X] =
  val seedsStr = seeds.map(p => s""""akka://agario@127.0.0.1:$p"""").mkString(", ")
  val configStr =
    s"""
       |akka.remote.artery.canonical.port = $port
       |akka.cluster.seed-nodes = [ $seedsStr ]
       |""".stripMargin

  val config = ConfigFactory
    .parseString(configStr)
    .withFallback(ConfigFactory.load(file))

  ActorSystem(root, file, config)



def startupWithRole[X](role: String, port: Int)(root: => Behavior[X]): ActorSystem[X] =
  val config = ConfigFactory
    .parseString(s"""
      akka.remote.artery.canonical.port=$port
      akka.cluster.roles = [$role]
      """)
    .withFallback(ConfigFactory.load("base-cluster"))

  // Create an Akka system
  ActorSystem(root, "ClusterSystem", config)
