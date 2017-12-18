(ns adventure.core
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str])
  (:gen-class))


(def the-map
  {
   :basement {:desc "Welcome to the small town of Hawkins! You are Mike Wheeler and your friends are Dustin, Lucas, and Will.
Dustin and Lucas are currently in your party, but Will has been captured by the demegorgan. You must
save him before it's too late!"
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
Type pickup_bike to grab Will's bike."
              :contents #{:bike}}
   :forest {:desc "The forest is a dangerous place at night. You, Dustin, and Lucas travel through the forest in
hopes of finding Will. It begins to storm and Lucas is getting nervous. You turn your flashlight towards a noise
you hear in the bushes... You've found Eleven!"
              :title "in the forest"
              :dir {:west :mirkwood, :south :store, :east :cliff}
              :people #{:eleven}
              :help "Type 'friend_eleven' to add eleven to your party. \nType 'west' to go to Mirkwood."
              :contents #{}}
   :house {:desc "The Byer's House is dark and empty. You hear sounds coming from Will's room. The lights start to flicker, and you
recognize the song 'Should I Stay or Should I Go'..."
              :title "in the Byer's House"
              :dir {:east :mirkwood}
              :people #{}
              :help "Type 'east' to go to Mirkwood."
              :contents #{:bat}}   
    ; ASSUME WE HAVE ELEVEN. DO NOT LET THEM HERE IF THEY DO NOT          
   :store {:desc "The grocery store is the place for food. We need to buy Eleven some food because she is hungry."
              :title "The grocery store"
              :dir {:north :forest}
              :people #{}
              :help "Type 'north' to go to forest."
              :contents #{:eggo_waffles, :frozen_pizza}}
   :cliff {:desc "You take a good look at the beatiful waterfall, and you realize this is one of the best views you have seen in your life. \n
                  Then you realize there is a massive cliff. You need to see if you can trust Eleven, so you must test her strength."
              :title "The Cliff"
              :dir {:west :forest}
              :people #{}
              :help "Type 'west' to go to forest.\n Type 'pickup_key' to pickup the key. \nType 'jump' to jump off the cliff"
              :contents #{:key}}
   :bedroom {:desc "The room is covered in striped wallpaper and pictures of Nancy's friends. On top of Nancy's bed
you find Steve Harrington fixing his hair. "
              :title "in Nancy's Bedroom"
              :dir {:downstairs :basement, :south :mirkwood}
              :people #{}
              :help "Type 'downstairs' to go to basement. \nType 'south' to go to Mirkwood."
              :contents #{}}})

(def adventurer
  {:location :basement
   :inventory #{}
   :party #{:Dustin, :Lucas}
   :tick 0
   :seen #{}})

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
    (if (nil? dest)
      (do (println "You can't go that way.")
          player)
      (assoc-in player [:location] dest))))


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

(defn help [player]
  (let [location (player :location)]
    (do (println (str (-> the-map location :help))) player)))


(defn tock [player]
  (update-in player [:tick] inc))

(defn respond [player command]
  (match command
         [:look] (update-in player [:seen] #(disj % (-> player :location)))
         (:or [:n] [:north] ) (go :north player)
         [:south] (go :south player)
         [:east] (go :east player)
         [:west] (go :west player)
         [:upstairs] (go :upstairs player)
         [:downstairs] (go :downstairs player)
         ;[:pickup_potato] (pickup :potato player)
         ;[:pickup_book] (pickup :book player)
         [:friend_eleven] (addparty :eleven player)
         [:status] (up player)
         [:help] (help player)

         _ (do (println "I don't understand you.")
               player)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (loop [local-map the-map
         local-player adventurer]
    (let [pl (status local-player)
          _  (println " What do you want to do?")
          command (read-line)]
      (recur local-map (respond pl (to-keywords command))))))
