(ns adventure.core
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str])
  (:gen-class))


(def the-map
  {
   :basement {:desc "Welcome to the small town of Hawkins! You are Mike Wheeler and your friends are Dustin, Lucas, and Will.
Dustin and Lucas are currently in your party, but Will has been captured by the demegorgan. You must
save him before it's too late! Type 'help' to see what you are able to do while you explore Hawkins. You can quit the game at
any time by typing 'quit'."
              :title "in your basement"
              :dir {:south :mirkwood, :upstairs :bedroom}
              :people #{}
              :help "Type 'south' to go to Mirkwood. \nType 'upstairs' to go to Nancy's bedroom."
              :contents #{}}

   :mirkwood {:desc "Mirkwood is a road that runs past the nearby forest. You see Will's bike lying on the side of the
road, abandoned."
              :title "on Mirkwood"
              :dir {:north :basement, :east :forest, :west :house}
              :people #{}
              :help "Type 'north' to go back to your basement. \nType 'east' to go to the forest. \nType 'west' to go to the Byer's House.
Type 'pickup bike' to grab Will's bike."
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

   :stayhouse {:desc "You head down the hallway to check out Will's room only to look to your right and see the demegorgan
stretching the wallpaper out in an attempt to break through the wall! At the same time a bat with nails sticking out of it
catches your eye. "
               :title "in the Byer's House"
               :dir {:leave :mirkwood}
               :people #{}
               :help "Type 'pickup bat' to grab the weapon.\nThen you must decide to run or fight the demegorgan!
Be careful... you might not be prepared for an unexpected battle...\n
Type 'fight' to try to defeat the demegorgan.\nType 'leave' to flee Will's house."}
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
              :dir {:west :forest}
              :people #{}
              :help "Type 'west' to go to the forest.\nType 'grab key' to grab the key from the cliff's ledge."
              :contents #{:key}}

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
    (println (str "You are " (-> the-map location :title) ". "))
    (println)
    (when-not ((player :seen) location)
      (print (-> the-map location :desc)))
    (update-in player [:seen] #(conj % location))))

(defn up [player]
  (let [location (player :location)]
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

    (if (and (= dest :store) (not (contains? (player :party) :eleven)))
        (do (println "Hmmm... The grocery store seems to be closed. Maybe if you had some help from a certain
super-powered friend you would be able to break in?") player)
        (if (and (= location :store) (not (player :eaten)))
          (do (println "Eleven is really hungry! She won't let you leave until you feed her.") player)
          (if (nil? dest)
            (do (println "You can't go that way.")
                player)
            (assoc-in player [:location] dest))))))


(defn pickup [contents player]
  (let [location (player :location)
              item (->> the-map location :contents contents)]
    (if (nil? item)
      (do (println "You can't pick that up.") player)
      (update-in player [:inventory] #(conj % item)))))

(defn addparty [people player]
  (let [location (player :location)
              person (->> the-map location :people people)]
    (if (nil? person)
      (do (println "There's no one here.") player)
      (update-in player [:party] #(conj % person)))))

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
    (if (= location :stayhouse)
      (do (println "You were not prepared to fight the demegorgan yet! The demegorgan frees itself from
the wall and bites your head off... and you die.") player))
    (if (= location :stayhouse)
      (update-in player [:health] #(- % 100)))))

(defn jump [dir player]
       (if (contains? (player :party) :eleven)
         (go :jump player)
         (println "Ahhhhhhhhhh.... *splash*... So it seems you forgot to add someone to your party who could have used
     some supernatural forces to prevent you from falling to your death. You died."))
       (if (not (contains? (player :party) :eleven))
         (update-in player [:health] #(- % 100))))

(defn tock [player]
  (update-in player [:tick] inc))

(def adventurer
  {:location :basement
   :inventory #{}
   :party #{:Dustin, :Lucas}
   :tick 0
   :health 100
   :eaten false
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

         _ (do (println "I don't understand you.")
               player)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (loop [local-map the-map
         local-player adventurer]
   (when (> (local-player :health) 0) ;exit game when u die
    (let [pl (status local-player)
          _  (println " What do you want to do?")
          command (read-line)]
      (recur local-map (respond pl (to-keywords command)))))))
