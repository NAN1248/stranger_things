(ns adventure.core
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str]
						[clj-audio.core :as audio] ; comment this count if sound doesn't work
																				)
  (:gen-class))

; The game's map
(def the-map
  {
  :basement {:desc "The basement is dark, and the Dungeons and Dragons game is still set up on the table."
            :title "in your basement"
            :dir {:south :mirkwood, :upstairs :bedroom}
            :people #{}
            :help "Type 'south' to go to Mirkwood. \nType 'upstairs' to go to Nancy's bedroom."
            :contents #{}}

  :mirkwood {:desc "Mirkwood is a road that runs past the nearby forest. You see Will's bike lying on the side of the road, abandoned."
            :title "on Mirkwood"
            :dir {:north :basement, :east :forest, :west :house}
            :people #{}
            :help "Type 'north' to go back to your basement. \nType 'east' to go to the forest. \nType 'west' to go to the Byer's House.\nType 'pickup bike' to grab Will's bike."
            :contents #{:bike}}

  :forest {:desc "The forest is a dangerous place at night. You, Dustin, and Lucas travel through the forest in
hopes of finding Will. It begins to storm and Lucas is getting nervous. You turn your flashlight towards a noise
you hear in the bushes... You've found Eleven!"
            :title "in the forest"
            :dir {:west :mirkwood, :south :store, :east :cliff}
            :people #{:eleven}
            :help "Type 'friend eleven' to add Eleven to your party. \nType 'west' to go to Mirkwood.
                    Type 'south' to go to the local Hawkins' grocery store.\nType 'east' to head towards the cliff overlooking the lake."
            :contents #{}}

  :house {:desc "The Byer's House is dark and empty. You hear sounds coming from Will's room. The lights start to flicker, and you
recognize the song 'Should I Stay or Should I Go'..."
            :title "in the Byer's House"
            :dir {:leave :mirkwood, :stay :stayhouse}
            :people #{}
            :help "Type 'leave' to go back to Mirkwood.\nType 'stay' to investigate and risk encountering whoever is playing the music."
            :contents #{}}

  :stayhouse {:desc "You head down the hallway to check out Will's room only to look to your right and see the demogorgan
stretching the wallpaper out in an attempt to break through the wall! At the same time a bat with nails sticking out of it
catches your eye. "
             :title "in the Byer's House"
             :dir {:leave :mirkwood}
             :people #{}
             :help "Type 'pickup bat' to grab the weapon.\nThen you must decide to run or fight the demogorgan!
(Be careful... you might not be prepared for an unexpected battle...)\nType 'fight' to try to defeat the demogorgan.\nType 'leave' to flee Will's house."}
            :contents #{:bat}

  :store {:desc "Eleven breaks the glass in the double doors with her mind and you step inside! Eleven complains that
she is hungry after escaping the D.O.E. Perhaps you should get her some food..."
            :title "at the grocery store"
            :dir {:north :forest}
            :people #{}
            :help "Type 'north' to go to the forest.
Type 'grab eggo' or 'grab pizza' to grab either the eggo waffles or the frozen pizza from the freezer isle.
Type 'eat eggo' or 'eat pizza' to have Eleven fuel up on some food :)"
            :contents #{:eggo_waffles, :frozen_pizza}}

  :cliff {:desc "You look around at how beautiful it is here on the edge of the cliff overlooking the lake. Over
the edge of the cliff you see what seems to be a silver key sitting on a ledge a few feet below you... Oh no!
You don't know how, but the bullies from school have found you here! They start to run towards you..."
            :title "at the Cliff"
            :dir {:west :forest, :jump :jumped}
            :people #{}
            :help "Type 'west' to go to the forest.\nType 'jump' to jump off the edge of the cliff."
            :contents #{}}

  :jumped {:desc "You jumped off the cliff! But Eleven used her mind powers to prevent you from falling to your
death! She has you suspended in the air, in arms reach of the key you saw earlier..."
            :title "at the Cliff"
            :dir {:west :forest, :north :bus}
            :people #{}
            :help "Type 'west' to go to the forest.\nType 'north' to go to the school bus.\nType 'grab key' to grab the key from the cliff's ledge."
            :contents #{:key}}
  ;
  ;
  ; note: hide and fight must be implemented
  ; fight should kill them "You dont stand a chance against the helicopter"
  ;
  :bus {:desc "You arrive at the abandoned school bus. In the distance you can hear a helicopter whirring. You know exactly who it is.
They want to take Eleven back to the lab. Will you hide and hope they pass by or fight and risk losing Eleven and your lives?"
            :title "at the abandoned school bus"
            :dir {:south :cliff, :west :station}
            :people #{}
            :help "Type 'south' to go to the cliff.\nType 'west' to go to the police station.\nType 'hide' to take evasive action.\nType 'fight' to take on the helicopter."
            :contents #{}}

  :survivedbus {:desc "You duck down behind the seats inside the abandoned school bus. Overhead the helicopter stalls... but after a time it moves on.
You've survived and kept Eleven safe!"
            :title "at the abandoned school bus"
            :dir {:south :cliff, :west :station}
            :people #{}
            :help "Type 'south' to go to the cliff.\nType 'west' to go to the police station."
            :contents #{}}

  :station {:desc "You arrive at the Police Station. In a chair you see a middle aged man who is complacent and curious, smoking his cigarrette and drinking a cold beer.
You can tell he has seen a lot in his time, and think he can help you out on your journey to find Will. His name is Hopper, and he is the chief of this town."
            :title "at the Police Station"
            :dir {:east :bus, :north :school, :south :lab}
            :people #{:hopper}
            :help "Type 'east' to go to the bus.\nType 'north' to go the school.\nType 'south' to go to the lab.\nType 'talk to hopper' to talk to Chief Hopper.
Type 'friend hopper' to add him to your party."
            :contents #{}}
  ;
  ;
  ; note: the school no longer connects to the lab
  ;
  ;
  :school {:desc "You have arrived at Hawkins Middle School. This establishment is where you study everyday - specifically in the AV club room.
You roam the halls looking for your AV Club teacher because you think he will have a good idea as to what to do about Will being stuck in another dimension.
You see him standing at the end of the hall."
            :title "at Hawkins Middle School"
            :dir {:east :bus, :west :basement, :south :station}
            :people #{:mr_clarke}
            :help "Type 'east' to go to the bus.\nType 'west' to go back to your basement.\nType 'south' to go to the police station.
Type 'talk to clarke' to talk to Mr. Clarke.\nType 'friend clarke' to add Mr. Clarke to your party."
            :contents #{}}

  :lab {:desc "You get past the gate, and there it is: Hawkins National Labatory
Known for being associated with the 'energy department' this laboratory is suspicious and you can sense that something is near.
Almost as if you were standing right on top of it...\n"
            :title "in the Lab"
            :dir {:downstairs :updown, :north :station}
            :people #{}
            :help "Type 'north' to go to the police station."
            :contents #{}}
  ;
  ;
  ; note: have hide kill them "you were found and eaten alive"
  ; we should only have them be able to see the go home option after the demegorgon has died
  ;
  :updown {:desc "The winds howl in the distance, and a chill goes down your spine. Everything is dark and covered in black vines.
You cannot see Will, but you sense that he is here. You cry out Will's name...but you are met with silence.
Suddenly out of the ground emerges the demogorgon. Your options are battle or hide.\n"
            :title "in the Upside Down"
            :dir {:battle :updownbattle, :west :basement, :fight, :hide}
            :people #{:will}
            :help "Type 'battle' to fight the demegorgon.\nType 'hide' to attempt to hide from the demegorgon."
            :contents #{}}

  :updownbattle {:desc "You have chosen to battle the demegorgan... good luck! These are the available attacks: 'swing bat' : 'throw rocks' : 'use eleven'"
            :title "in the Upside Down"
            :dir {}
            :people #{:will}
            :help "You're in the Upside Down... no one can help you anymore...
These are the available attacks: 'swing bat' : 'throw rocks' : 'use eleven'"
            :contents #{}}


  :bedroom {:desc "The room is covered in striped wallpaper and pictures of Nancy's friends. On top of Nancy's bed
you find Steve Harrington fixing his hair. "
            :title "in Nancy's Bedroom"
            :dir {:downstairs :basement, :south :mirkwood}
            :people #{:steve}
            :help "Type 'downstairs' to go to basement. \nType 'south' to go to Mirkwood.
                    Type 'friend steve' to add Steve to your party."
            :contents #{}}})

(defn status [player]
  (let [location (player :location)]
    (println)
    (println (str "You are " (-> the-map location :title) ". "))
    (when-not ((player :seen) location)
      (print (-> the-map location :desc)))
    (update-in player [:seen] #(conj % location))))

(defn up [player]
  (let [location (player :location)]
    (println)
    (println "Player Status ----->>")
    (println (str "You are " (-> the-map location :title) ". "))
    (println (str "In your bag: " (player :inventory) " "))
    (println (str "Your party consists of: " (player :party) " "))
    (println)
    (when-not ((player :seen) location)
      (print (-> the-map location :desc)))
    (update-in player [:seen] #(conj % location))))

(defn to-keywords [commands]
  (mapv keyword (str/split commands #"[.,?! ]+")))

(defn go [dir player]
  (let [location (player :location)
        dest (->> the-map location :dir dir)]

    (if (and (not (contains? (player :party) :hopper)) (= dest :lab))
      (do (println "Without Hopper, getting into the lab seems hopeless...") player)

    (if (and (contains? (player :party) :eleven) (and (not (= dest :store)) (and (= location :forest) (not (player :eaten)))))
      (do (println "Eleven is really hungry! She won't let you leave until you feed her.") player)

    (if (and (= location :bus) (not (player :ducked)))
        (do (println "The helicopter isn't going away any time soon! You have to decide how to deal with it first...") player)

    (if (and (and (or (= dest :forest) (= dest :house)) (= location :mirkwood)) (not (contains? (player :inventory) :bike)))
      (do (println "You might need some faster transportation to reach those places...") player)


    (if (and (= dest :store) (not (contains? (player :party) :eleven)))
        (do (println "Hmmm... The grocery store seems to be closed. Maybe if you had some help from a certain
super-powered friend you would be able to break in?") player)

        (if (and (= location :store) (not (player :eaten)))
          (do (println "Eleven is really hungry! She won't let you leave until you feed her.") player)

          (if (nil? dest)
            (do (println "You can't go that way.")
                player)
            (assoc-in player [:location] dest))))))))))


(defn pickup [contents player]
  (let [location (player :location)
              item (->> the-map location :contents contents)]
    (if (nil? item)
      (do (println "You can't pick that up.") player)
      (update-in player [:inventory] #(conj % item)))))

(defn addparty [people player]
  (let [location (player :location)
              person (->> the-map location :people people)]
    (if (= people :clarke)
      (do (println "Sorry kids I cannot come with you. I have classes to teach!") player)
    (if (nil? person)
      (do (println "There's no one here.") player)
      (update-in player [:party] #(conj % person))))))

(defn eat [food player]
  (if (and (contains? (player :inventory) :eggo_waffles) (= food :eggo_waffles))
      (do (println "'Yum' -eleven") player)
      (if (and (contains? (player :inventory) :frozen_pizza) (= food :frozen_pizza))
          (do (println "Uh-oh it looks like eleven doesn't like frozen pizza very much...") player)
          (do (println "Hmmm it doesn't look like you've grabbed any food for her yet.") player)))

  (if (and (contains? (player :inventory) :eggo_waffles) (= food :eggo_waffles))
    (assoc-in player [:eaten] true)))

(defn help [player]
  (let [location (player :location)]
    (do (println (str (-> the-map location :help))) player)))

(defn fight [player]
  (let [location (player :location)]

  (if (= location :bus)
    (do (println "You step out of the abandoned school bus to face the helicopter. You put up your arms and clench your fists
in an effort to show you are ready to fight. The people in the helicopter immediately take you out,
kidnap Eleven, and everybody else dies. Helicopter >> you.") player))

    (if (= location :stayhouse)
      (do (println "You were not prepared to fight the demogorgan yet! The demogorgan frees itself from
the wall and bites your head off... and you die.") player))

    (if (or (= location :stayhouse) (= location :bus))
      (update-in player [:health] #(- % 100)))))

(defn jump [dir player]
      (if (not (contains? (player :party) :eleven))
        (println "Ahhhhhhhhhh.... *splash*... So it seems you forgot to add someone to your party who could have used
    some supernatural forces to prevent you from falling to your death. You died."))
      (if (not (contains? (player :party) :eleven))
        (update-in player [:health] #(- % 100))
        (go :jump player)))



(defn hide [player]
  (let [location (player :location)]

  (if (= location :bus)
  (println "You duck down behind the seats inside the abandoned school bus. Overhead the helicopter stalls... but after a time it moves on.
You've survived and kept Eleven safe!")
    (if (= location :updown)
      (println "You've attempted to hide from the demagorgan! But you are no longer in Hawkins... you are
in the Demegorgan's home. So he finds you easily and kills you. However, you came really close to finding Will
before your tragic death.")))

  (if (= location :bus)
    (assoc-in player [:ducked] true)
    (if (= location :updown)
      (update-in player [:health] #(- % 100))))

))


(defn swingbat [player]

(if (and (> (player :demhealth) 15) (and (contains? (player :party) :steve) (contains? (player :inventory) :bat)))
  (println (str "Steve swings the nail covered bat at the demegorgan and knocks his health down 15! New demagorgan health: " (- (player :demhealth) 15)))
  (if (< (player :demhealth) 16)
  (println "The demegorgan is almost dead! Use your most powerful weapon to finish him off! But the demagorgan takes advantage
of your distraction and knocks your health down 10!")
  (println (str "You are either missing Steve from your party or you do not have the bat from the Byer's House... The demagorgan takes advantage
of your distraction and knocks your health down 10! Your health: " (- (player :health) 10)))
))

(if (< (player :health) 1)
  (println "Andddddddd you died."))

(if (and (> (player :demhealth) 15) (and (contains? (player :party) :steve) (contains? (player :inventory) :bat)))
(update-in player [:demhealth] #(- % 15))
(update-in player [:health] #(- % 10))

))

(defn throrocks [player]

  (if (> (player :demhealth) 15)
    (println (str "Lucas and Dustin throw rocks at the Demegorgan and knock his health down 5! New demagorgan health: " (- (player :demhealth) 5)))
    (println (str "The demegorgan is almost dead! Use your most powerful weapon to finish him off! The demagorgan takes advantage
of your distraction and knocks your health down 10! Your health: " (- (player :health) 10)))
  )

  (if (< (player :health) 1)
    (println "Andddddddd you died."))

  (if (> (player :demhealth) 15)
  (update-in player [:demhealth] #(- % 5))
  (update-in player [:health] #(- % 10))

))

(defn useeleven [player]
  (if (and (< (player :demhealth) 16) (contains? (player :party) :eleven))
    (println "Eleven finishes off the demagorgan and he is vaporized!!
\n*********** :) :) ! ! ! CONGRATULATIONS ! ! ! (: (: ***********
You have succesfully defeated the Demegorgan!! From a distance you see Will crawling towards you. You run to him,
pick him up, and swing him over your shoulder. With your party you leave the Upside Down and go home to celebrate!\n")
    (println (str "Save up your most powerful weapon for when the demagorgan is almost dead! The demagorgan takes advantage
of your distraction and knocks your health down 10! Your health: " (- (player :health) 10)))
  )

  (if (< (player :health) 1)
    (println "Andddddddd you died."))

  (if (and (< (player :demhealth) 16) (contains? (player :party) :eleven))
  (update-in player [:demhealth] #(- % 100))
  (update-in player [:health] #(- % 10))
))

(defn talk [person player]
  (if (= person :clarke)
    (do (println "Oh hey kids what's going on?...
...
Oh your looking for will?...
...
You think he's in another dimension?....
...
Well I think you guys will find him! Just, if you feel any weird gravity waves you should try going 'downstairs'...
...
Good luck!\n") player)
    (if (= person :hopper)
      (do (println "Hey kids, you got a problem?
...
Uh huh, so you say he's missing?
...
Those government agents keep ruining things around here.
...
Well if you think Will is in the Lab, I happen to know a secret way in.
...") player))
))

(defn tock [player]
  (update-in player [:tick] inc))

(def adventurer
  {:location :station
   :inventory #{}
   :party #{:Dustin, :Lucas}
   :tick 0
   :health 100
   :eaten false
   :ducked false
   :demhealth 100
   :seen #{}})

(defn respond [player command]
  (match command
         [:look] (update-in player [:seen] #(disj % (-> player :location)))
         ;directional movements
         [:north]  (go :north player)
         [:south] (go :south player)
         [:east] (go :east player)
         [:west] (go :west player)
         [:upstairs] (go :upstairs player)
         [:downstairs] (go :downstairs player)
         [:leave] (go :leave player)
         [:stay] (go :stay player)
         [:battle] (go :battle player)
         ;picking up objects and adding them to the player's inventory
         [:pickup :bike] (pickup :bike player)
         [:pickup :bat] (pickup :bat player)
         [:pickup :key] (pickup :key player)
         [:grab :eggo] (pickup :eggo_waffles player)
         [:grab :pizza] (pickup :frozen_pizza player)
         [:grab :key] (pickup :key player)
         ;adding people found in the game to your current party
         [:friend :eleven] (addparty :eleven player)
         [:friend :steve] (addparty :steve player)
         [:friend :clarke] (addparty :clarke player)
         [:friend :hopper] (addparty :hopper player)
         ;eating functionality given to eleven
         [:eat :pizza] (eat :frozen_pizza player)
         [:eat :eggo] (eat :eggo_waffles player)
         ;commands that give the player information or whole game commands
         [:status] (up player)
         [:help] (help player)
         [:quit] (update-in player [:health] #(- % 100))
         ;battle commands
         [:fight] (fight player)
         [:jump] (jump :jump player)
         [:hide] (hide player)
         [:swing :bat] (swingbat player)
         [:throw :rocks] (throrocks player)
         [:use :eleven] (useeleven player)
         ;talking functions
         [:talk :to :clarke] (talk :clarke player)
         [:talk :to :hopper] (talk :hopper player)

         _ (do (println "I don't understand you.")
               player)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]

  (println)
  (println "********************************************************************")
  (println "************************** STRANGER THINGS *************************")
  (println "********************* A CLOJURE ADVENTURE GAME *********************")
  (println "*************************** CREATED BY: ****************************")
  (println "********************** TORI & NATHAN & NAVID ***********************")
  (println "********************************************************************")
  (println)
  (println "Welcome to the small town of Hawkins! You are Mike Wheeler and your friends are Dustin, Lucas, and Will.
Dustin and Lucas are currently in your party, but Will has been captured by the demogorgan. You must
save him before it's too late! Type 'help' to see what you are able to do while you explore Hawkins.
You can quit the game at any time by typing 'quit'. And lastly, *turn on your sound* there is music playing!")
  (println)

	(audio/loop-clip (audio/clip (audio/->stream "./stmt.wav"))) ; comment this count if sound doesn't work

  (loop [local-map the-map
         local-player adventurer]
   (when (and (> (local-player :demhealth) 0) (> (local-player :health) 0)) ;exit game when u die
    (let [pl (status local-player)
          _  (println " What do you want to do?")
          command (read-line)]
      (recur local-map (respond pl (to-keywords command))))))
  (println)
  (println "*****************************************************")
  (println "********************* GAME OVER *********************")
  (println "*****************************************************")
  (println)
  (System/exit 0)) ; hard exit nessecary to stop the sound here!
