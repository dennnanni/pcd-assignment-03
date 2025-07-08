package main

import (
	"fmt"
	"math/rand"
	"time"
)

type Move string

const (
	Rock     Move = "Rock"
	Paper    Move = "Paper"
	Scissors Move = "Scissors"
)

var moves = []Move{Rock, Paper, Scissors}

// Result indicates who won: 0 = tie, 1 = player1, 2 = player2
func judge(p1 Move, p2 Move) int {
	switch {
	case p1 == p2:
		return 0
	case (p1 == Rock && p2 == Scissors) || (p1 == Paper && p2 == Rock) || (p1 == Scissors && p2 == Paper):
		return 1
	default:
		return 2
	}
}

type Request struct {
	reply chan Move
}

func player(name string, req chan Request, results chan string) {
	score := 0
	for {
		reply := make(chan Move)
		req <- Request{reply: reply}
		move := moves[rand.Intn(len(moves))]
		reply <- move

		result := <-results
		if result == "win" {
			score++
		}
		fmt.Printf("[Player %s] Move: %s | Result: %s | Score: %d\n", name, move, result, score)
	}
}

func referee(p1Req, p2Req chan Request, p1Res, p2Res chan string) {
	for round := 1; ; round++ {
		// Ask moves
		req1 := <-p1Req
		req2 := <-p2Req

		move1 := <-req1.reply
		move2 := <-req2.reply

		winner := judge(move1, move2)
		fmt.Printf("\n[Round %d] Player1: %s vs Player2: %s --> ", round, move1, move2)

		switch winner {
		case 0:
			fmt.Println("Draw")
			p1Res <- "draw"
			p2Res <- "draw"
		case 1:
			fmt.Println("Player1 wins")
			p1Res <- "win"
			p2Res <- "lose"
		case 2:
			fmt.Println("Player2 wins")
			p1Res <- "lose"
			p2Res <- "win"
		}
		time.Sleep(1 * time.Second)
	}
}

func main() {
	p1Req := make(chan Request)
	p2Req := make(chan Request)
	p1Res := make(chan string)
	p2Res := make(chan string)

	go player("1", p1Req, p1Res)
	go player("2", p2Req, p2Res)
	referee(p1Req, p2Req, p1Res, p2Res)
}
