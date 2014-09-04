(ns QuoteBot-app.core
  (:require [twitter.oauth :refer :all]
            [twitter.api.restful :refer :all]
            [clj-time.core :refer (hour minute)]
            [clj-time.local :refer (local-now)]
            [clj-time.format :as tf]
            [clojure.string :refer (split-lines)]))

;;
;; Quote/Lyric stuff
;;

(def quotes ["Everyday, I wake up, we drink a lot of coffee and watch the CNN"
             "What if I told you they been lying about that double wide with water rights?"
             "Outside is an army of antlers!"
             "In the north they call us rebels, in the south they call us yankees, because every other sucker's born to do the hokey pokey"
             "...and out from the belly of a whale came a prophet, Amen!"
             "\"Tastes just like chicken,\" they say!"
             "Lobsterbacks attack the town again!"
             "Oh, no, fishin' ain't what it used to be! I've had some bad years but this one is just killing me!"
             "Tipping cows in fields elysian!"
             "Bang bang bang bang! Vamanos, vamanos!"
             "Peace keeping agents from several futures... they got a warrant out - some judge's letter..."
             "Ribonucleic acid freak out! The power of prayer! The long halls of science and all the lunatics committed there!"
             "Anthrax, ham radio, and liquor!"
             "Many people tell me my mind is a ghetto. Obviously they've been gentrified."
             "A man asked me for a dollar, I asked him what it's for.  He said \"I have seen them,\" I said \"Ok, it's yours\""
             "In the beginning there was void, The Face woke up, then there was noise."
             "Fire and water!  Heaven and stone. Radio kings. Hammer and throne."
             "Roll, roll down highways with a vengeance, YEAH!"
             "Hey kid, are you going my way? Hop in, we'll have ourselves a field day!"
             "Robot lords of Tokyo: Smile! Taste kittens!"
             "Please allow me to adjust my pants so I can dance the good time dance and put the onlookers and innocent bysteanders into a trance"
             "Uncounted Les Pauls ascend to the sky! Where there was darkness, now only light!"
             "God names man, man names ape, flight of Icarus, down into flames."
             "Dog men to the deck, there's a hooker on the..."
             "Never trust the white man driving the black van! He's been saving all his voodoo for you.... just for you!"
             "Every time I open my window, cranes fly in to terrorize me!"
             "We've got Greedo! Solo to the rear!"
             "Among the metal ones a messenger will soon arrive."
             "You want someone to talk to? Well, I'm your man! I've seen it all, and I know where you live!"
             "You want some more information? There's a buzzkiller in the room!"])


(defn get-quote-after
  "Get the quote after the given quote. If no quote is given, get the 1st quote"
  ([] (first quotes))
  ([prev-quote] (nth quotes 
                     (mod (inc (.indexOf quotes prev-quote)) (count quotes)))))


;;
;; Twitter stuff
;; 

(def twitter-cred-file "/home/bhu/twitter_creds")


(defn init-twitter
  "Initialize twitter stuff and return a handle for it"
  []
  (let [auth-keys (split-lines (slurp twitter-cred-file))]
    (make-oauth-creds (first auth-keys)  ;; Find better way!
                                    (second auth-keys)
                                    (nth auth-keys 2)
                                    (nth auth-keys 3))))


(defn get-last-tweet-text
  "Return the text for the latest tweet"
  [handle]
  (-> (statuses-user-timeline :oauth-creds handle :params { :count 1 }) 
    :body first :text))


(defn send-tweet
  "Send a new tweet"
  [handle text]
  (statuses-update :oauth-creds handle 
                   :params { :status text }))

;;
;; Scheduler 
;;
(def time-formatter (tf/formatters :mysql))
(def delay-1min (* 1000 60))

(defn wait-for-time
  [h m]
  "Sleep until the local time is equal to the given hour and minute"
  (println "Waiting for" h ":" m)
  (loop [now (local-now)]
    (if (and (= (minute now) m) (= (hour now) h))
      (println "It's finally" (hour now) ":" (minute now))
      (do 
        (println "It's only" (hour now) ":" (minute now))
        (Thread/sleep delay-1min)
        (recur (local-now))))))


;;
;; Core program
;; 

(defn tweet-next-quote
  "Get the next quote and tweet it"
  [handle]
  (send-tweet handle (get-quote-after (get-last-tweet-text handle))))


(defn -main
  [& args]
  (let [twitter-handle (init-twitter)]
    (while true
      (wait-for-time 22 3)
      (tweet-next-quote twitter-handle)
      (wait-for-time 10 0)
      (tweet-next-quote twitter-handle))))


