(ns game.core)

(defonce canvas (.getElementById js/document "cv"))
(defonce ctx (.getContext canvas "2d"))

(def UP 38)
(def SPACE 32)

(def screen-width 400)
(def screen-height 400)

(def player-x 100)
(def jump-speed -15)
(def player-size 10)
(def ground-y (- screen-height 20))
(def obstacle-width 10)
(def speed 4)
(def obstacle-time 60)

(def init-state
  {:player-y ground-y
   :player-v 0
   :obstacle-timer obstacle-time
   :obstacles '()})

(defonce game-state (atom init-state))

(defn on-key-down [evt]
  (let [kc (.-keyCode evt)]
    (when (and (or (= kc UP) (= kc SPACE)) (= (:player-y @game-state) ground-y))
      (swap! game-state assoc :player-v jump-speed))))

(.addEventListener js/document "keydown" on-key-down)

(defn update-state [{:keys [player-v] :as state}]
  (-> state
      (update :obstacles (partial map (fn [[x h]] [(- x speed) h])))
      (update :player-y (partial + player-v))
      (update :player-v inc)
      (update :obstacle-timer dec)
      (update :obstacles (partial filter (fn [[x _]] (> x (- obstacle-width)))))
      (as-> s
          (cond-> s
            (> (:player-y s) ground-y) (assoc :player-y ground-y)
            (< (:obstacle-timer s) 0) (assoc :obstacles (cons [screen-width 50]
                                                              (:obstacles s) )
                                             :obstacle-timer 60)))))

(defn draw [{:keys [obstacles player-y]}]
  ;; Clear background
  (set! (.-fillStyle ctx) "white")
  (.fillRect ctx 0 0 screen-width screen-height)

  ;; Draw obstacles
  (set! (.-fillStyle ctx) "rgb(200, 0, 0)")
  (doseq [[x height] (:obstacles @game-state)]
    (.fillRect ctx x (+ (- ground-y height) player-size) obstacle-width height))

  ;; Draw ground
  (.fillRect ctx 0 (+ ground-y player-size) screen-width 2)

  ;; Draw player
  (.fillRect ctx player-x (:player-y @game-state) player-size player-size))

(defn collide [[x h] player-y]
  (not (or (> x (+ player-x player-size))
           (< (+ x obstacle-width) player-x)
           (< player-y (- ground-y h)))))

(defn main-loop []
  (swap! game-state update-state)
  (doseq [o (:obstacles @game-state)]
    (when (collide o (:player-y @game-state))
      (reset! game-state init-state)))
  (draw @game-state))

(defonce interval-id (js/setInterval main-loop (/ 1000 60)))
