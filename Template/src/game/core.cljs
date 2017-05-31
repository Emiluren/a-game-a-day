(ns game.core)

(defonce canvas (.getElementById js/document "cv"))
(defonce ctx (.getContext canvas "2d"))

(def LEFT 37)
(def UP 38)
(def RIGHT 39)
(def DOWN 40)

(defn init-state []
  {:px 10
   :py 10})

(defonce game-state (atom (init-state)))

(defn on-key-down [evt]
  (let [kc (.-keyCode evt)]
    (cond
      (= LEFT kc) (swap! game-state update :px #(- % 10))
      (= RIGHT kc) (swap! game-state update :px #(+ % 10))
      (= UP kc) (swap! game-state update :py #(- % 10))
      (= DOWN kc) (swap! game-state update :py #(+ % 10)))))

(.addEventListener js/document "keydown" on-key-down)

(defn update-game []
  (set! (.-fillStyle ctx) "rgb(200, 0, 0)")
  (.fillRect ctx (:px @game-state) (:py @game-state) 10 10))

(js/setInterval update-game (/ 1000 15))
