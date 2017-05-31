(ns game.core)

(defonce canvas (.getElementById js/document "cv"))
(defonce ctx (.getContext canvas "2d"))

(def LEFT 37)
(def UP 38)
(def RIGHT 39)
(def DOWN 40)

(def gs 20)
(def tc 20)

(defn init-state []
  {:px 10, :py 10,
   :ax 15, :ay 15,
   :xv 0, :yv 0,
   :trail [], :tail 5})

(defonce game-state (atom (init-state)))

(defn on-key-down [evt]
  (let [kc (.-keyCode evt)]
    (cond
      (= LEFT kc) (swap! game-state assoc :xv -1 :yv 0)
      (= RIGHT kc) (swap! game-state assoc :xv 1 :yv 0)
      (= UP kc) (swap! game-state assoc :xv 0 :yv -1)
      (= DOWN kc) (swap! game-state assoc :xv 0 :yv 1))))

(.addEventListener js/document "keydown" on-key-down)

(defn update-state [state]
  (-> state
      (update :px + (:xv state))
      (update :py + (:yv state))
      (cond-> (< (:px state) 0) (assoc :px (- tc 1))
              (> (:px state) (- tc 1)) (assoc :px 0)
              (< (:py state) 0) (assoc :py (- tc 1))
              (> (:py state) (- tc 1)) (assoc :py 0))))

(defn snake-eat [state]
  (-> state
      (update :tail inc)
      (assoc :ax (.floor js/Math (* (.random js/Math) tc))
             :ay (.floor js/Math (* (.random js/Math) tc)))))

(defn draw-state [{:keys [px py ax ay trail tail]}]
  (set! (.-fillStyle ctx) "black")
  (.fillRect ctx 0 0 400 400)

  (set! (.-fillStyle ctx) "lime")
  (doseq [[x y] trail]
    (.fillRect ctx (* x gs) (* y gs) (- gs 2) (- gs 2))
    (when (and (= x px) (= y py))
      (swap! game-state assoc :tail 5)))

  (swap! game-state #(-> %
                         (update :trail (partial cons [px py]))
                         (cond-> (> (count trail) tail)
                             (update :trail (partial take tail)))))

  (when (and (= ax px) (= ay py))
    (swap! game-state snake-eat))

  (set! (.-fillStyle ctx) "red")
  (.fillRect ctx (* ax gs) (* ay gs) (- gs 2) (- gs 2)))

(defn game-loop []
  (swap! game-state update-state)
  (draw-state @game-state))

(js/setInterval game-loop (/ 1000 15))
