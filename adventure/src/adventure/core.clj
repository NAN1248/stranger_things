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
              :contents #{:potato}}
   :mirkwood {:desc "Mirkwood is a road that runs past the nearby forest. You see Will's bike lying on the side of the
road, abandoned. "
              :title "on Mirkwood"
              :dir {:north :basement, :east :forest, :west :house}
              :contents #{}}
   :forest {:desc "The forest is a dangerous place at night. You, Dustin, and Lucas travel through the forest in
   hopes of finding Will. It begins to storm and Lucas is getting nervous. You turn your flashlight towards a noise
   you hear in the bushes... You've found Eleven! "
              :title "in the forest"
              :dir {:west :mirkwood}
              :contents #{}}
   :house {:desc "The Byer's House is gloomy without Will there."
              :title "in the Byer's House"
              :dir {:east :mirkwood}
              :contents #{}}
   :bedroom {:desc "The room is covered in striped wallpaper and pictures of Nancy's friends. On top of Nancy's bed
you find Steve Harrington fixing his hair. "
              :title "in Nancy's Bedroom"
              :dir {:downstairs :basement, :south :mirkwood}
              :contents #{}}})


(def adventurer
  {:location :basement
   :inventory #{}
   :tick 0
   :seen #{}})

(defn status [player]
  (let [location (player :location)]
    (println (str "You are " (-> the-map location :title) ". "))
    (println (str "In your bag: " (player :inventory) " --> "))
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

    ;  (defn pick [object player]
    ;    (let [location (player :location)]
    ;     (if (nil? the-map location :contents)
    ;       (do (println "No objects to pick up. ")
    ;         player
    ;       (update-in player [:inventory] #(conj % object))
    ;       (do (println "you have picked up an object"))))))

(defn pickup )


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
         [:pickup] (pickup :pickup player)

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
