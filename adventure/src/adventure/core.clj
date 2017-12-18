(ns adventure.core
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str])
  (:gen-class))


(def the-map
  {
   :basement {:desc "Welcome to the small town of Hawkins! You are Mike Wheeler and your friends are Dustin, Lucas, and Will.
    Dustin and Lucas are currently in your party, but Will has been captured by the demegorgan. You must
    save him before it's too late! "
              :title "in your basement"
              :dir {:south :mirkwood, :upstairs :bedroom}
              :people #{}
              :contents #{}}
   :mirkwood {:desc "Mirkwood is a road that runs past the nearby forest. You see Will's bike lying on the side of the
road, abandoned. "
              :title "on Mirkwood"
              :dir {:north :basement, :east :forest, :west :house}
              :people #{}
              :contents #{:bike}}
   :forest {:desc "The forest is a dangerous place at night. You, Dustin, and Lucas travel through the forest in
   hopes of finding Will. It begins to storm and Lucas is getting nervous. You turn your flashlight towards a noise
   you hear in the bushes... You've found Eleven! "
              :title "in the forest"
              :dir {:west :mirkwood}
              :people #{:eleven}
              :contents #{}}
   :house {:desc "The Byer's House is dark and empty."
              :title "in the Byer's House"
              :dir {:east :mirkwood}
              :people #{}
              :contents #{:bat}}
   :bedroom {:desc "The room is covered in striped wallpaper and pictures of Nancy's friends. On top of Nancy's bed
you find Steve Harrington fixing his hair. "
              :title "in Nancy's Bedroom"
              :dir {:downstairs :basement, :south :mirkwood}
              :people #{}
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

(defn update [player]
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
         [:status] (update player)

         _ (do (println "I don't understand you.")
               player)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (loop [local-map the-map
         local-player adventurer]
    (let [pl (status local-player)
          _  (println "What do you want to do?")
          command (read-line)]
      (recur local-map (respond pl (to-keywords command))))))
