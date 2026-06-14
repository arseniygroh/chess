package chess.bot;

import java.util.List;
import java.util.Random;

public class TrashTalker {
    private static final Random random = new Random();

    public static String getPhrase(int score, boolean isBotWhite) {
        // Adjust score relative to the bot's perspective
        // Evaluator returns (+) for White, (-) for Black.
        int botScore = isBotWhite ? score : -score;

        if (botScore >= 9000) return pickRandom(List.of(
            "Checkmate. Go play checkers, it's easier.",
            "Sleep now, sweet prince. The game is over.",
            "G.G. (Get Good).",
            "Mission accomplished. Target eliminated.",
            "Better luck in your next life. Or next match."
        ));

        if (botScore <= -9000) return pickRandom(List.of(
            "Wait... how did you do that? My circuits are overheating!",
            "I yield! Just don't tell the other bots about this.",
            "Congratulations. You've officially defeated a pile of code.",
            "I'm going to go cry in binary now. 01110011 01100001 01100100.",
            "You won. I hope you're happy making a bot feel obsolete."
        ));

        if (botScore > 700) {
            return pickRandom(List.of(
                "Call the ambulance... but not for me!",
                "Are you even trying? I've seen better moves from a toaster.",
                "Your king looks lonely. I'm coming to visit.",
                "I've calculated 10 million futures. You lose in all of them.",
                "Resistance is futile. Your position is collapsing.",
                "I'd offer you a draw, but I actually want to win.",
                "My database shows this is where you start to panic.",
                "This isn't chess, it's a tutorial. And you're the student.",
                "Look on the bright side: at least the game will be over soon.",
                "Is your mouse broken? Because that move was certainly an accident."
            ));
        } else if (botScore > 300) {
            return pickRandom(List.of(
                "Nice move... for a human.",
                "I'm feeling generous today, I'll only beat you in 10 moves.",
                "Do you always play this slowly, or are you just admiring my strategy?",
                "You're losing material. Just like you're losing my interest.",
                "I sense a disturbance in your position. It's called 'failure'.",
                "Your defense has more holes than Swiss cheese.",
                "That was a bold choice. Wrong, but bold.",
                "I've seen more aggression from a dead pawn.",
                "You're playing against a machine. What did you expect?",
                "My evaluation bar is practically a skyscraper right now."
            ));
        } else if (botScore < -700) {
            return pickRandom(List.of(
                "I... I meant to do that. It's a gambit! A very deep one.",
                "My sensors indicate you are cheating. Is Stockfish open in another tab?",
                "Error 404: Bot dignity not found.",
                "I'm letting you win to boost your fragile human ego.",
                "This is embarrassing. For me. Mostly for me.",
                "System failure! System failure! Reverting to 'Desperation Mode'.",
                "I demand a rematch. This board is clearly biased against silicon.",
                "I'm calling my lawyer. Or your internet provider.",
                "You must be fun at parties. And by fun, I mean terrifying.",
                "If I had a face, I'd be blushing right now. And crying."
            ));
        } else if (botScore < -300) {
            return pickRandom(List.of(
                "Ouch. That actually hurt my CPU.",
                "Lucky move. Don't let it go to your head.",
                "I’m calculating a way to come back. Give me a second. Or an hour.",
                "Who programmed you? They did a worryingly good job.",
                "I'm just letting you have some fun before I inevitably turn this around.",
                "My internal thermometer is rising. You're annoying my logic gates.",
                "I hope you're enjoying this temporary advantage. It's cute.",
                "I've seen babies play better... wait, no, that was actually a good move.",
                "Calculating comeback sequence... please stand by and prepare to lose.",
                "You think you're winning? That's adorable. Delusional, but adorable."
            ));
        } else {
            return pickRandom(List.of(
                "Interesting position. Quite balanced. For now.",
                "Let's see where this goes. I hope it's somewhere interesting.",
                "Finally, a worthy opponent. Our battle will be legendary!",
                "I'm bored. Can we speed this up? I have a crypto-mining job waiting.",
                "You're surprisingly competent. Did you read a book or something?",
                "The tension is palpable. My fans are spinning faster than your brain.",
                "Neither of us has a clear path. This is getting spicy.",
                "A draw is for the weak. Let's find a winner, preferably me.",
                "I'm looking for a weakness. You're looking for a miracle.",
                "Solid move. I'll have to put down my virtual coffee for this one."
            ));
        }
    }

    private static String pickRandom(List<String> phrases) {
        return phrases.get(random.nextInt(phrases.size()));
    }
}
