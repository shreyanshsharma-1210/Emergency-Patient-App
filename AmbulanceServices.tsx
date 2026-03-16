import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { FloatingSidebar } from "@/components/FloatingSidebar";
import { FloatingTopBar } from "@/components/FloatingTopBar";
import { useSidebar } from "@/contexts/SidebarContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { AlertTriangle, Phone, Siren, Ambulance as AmbulanceIcon, ShieldAlert, FlameKindling, MapPin, Navigation, Bell, Trash2 } from "lucide-react";

const frostedCardClass =
  "rounded-3xl border border-white/45 bg-gradient-to-br from-white/85 via-white/50 to-white/25 backdrop-blur-xl shadow-[0_30px_80px_rgba(59,130,246,0.18)]";

const HOSPITAL_POS: [number, number] = [22.7196, 75.8577]; // Indore (mock hospital)
const AMBULANCE_START: [number, number] = [22.7100, 75.8500];

// Fix Leaflet default markers
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

// Custom icons
const hospitalIcon = L.divIcon({
  html: '<div style="background: white; border-radius: 50%; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(0,0,0,0.3); font-size: 16px; color: #dc2626;">🏥</div>',
  className: '',
  iconSize: [30, 30],
  iconAnchor: [15, 15],
});

const ambulanceIcon = L.divIcon({
  html: '<div style="background: white; border-radius: 50%; width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(0,0,0,0.3); font-size: 18px; border: 2px solid #059669;">🚑</div>',
  className: '',
  iconSize: [32, 32],
  iconAnchor: [16, 16],
});

const userIcon = L.divIcon({
  html: '<div style="background: #3b82f6; border-radius: 50%; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(0,0,0,0.3); border: 3px solid white;">📍</div>',
  className: '',
  iconSize: [20, 20],
  iconAnchor: [10, 10],
});

// Custom Leaflet Map Component
interface LeafletMapProps {
  userPos: [number, number] | null;
  hospitals: { id: string; name: string; pos: [number, number]; distance: string; phone: string }[];
  ambulances: { id: string; pos: [number, number]; base: [number, number] }[];
  onAmbulanceBook: (ambulance: { id: string; pos: [number, number]; base: [number, number] }) => void;
  routePath: [number, number][] | null;
}

function LeafletMap({ userPos, hospitals, ambulances, onAmbulanceBook, routePath }: LeafletMapProps) {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<L.Map | null>(null);
  const markersRef = useRef<L.Marker[]>([]);
  const routeLineRef = useRef<L.Polyline | null>(null);

  // Initialize map
  useEffect(() => {
    if (!mapRef.current || mapInstanceRef.current) return;

    const map = L.map(mapRef.current).setView(userPos || HOSPITAL_POS, 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    mapInstanceRef.current = map;

    return () => {
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }
    };
  }, []);

  // Update markers when data changes
  useEffect(() => {
    if (!mapInstanceRef.current) return;

    // Clear existing markers
    markersRef.current.forEach(marker => {
      mapInstanceRef.current?.removeLayer(marker);
    });
    markersRef.current = [];

    // Add user location
    if (userPos) {
      const userMarker = L.marker(userPos, { icon: userIcon })
        .addTo(mapInstanceRef.current)
        .bindPopup('<div style="text-align: center;"><strong>Your Location</strong><br/>📍 You are here</div>');
      markersRef.current.push(userMarker);
    }

    // Add hospitals
    hospitals.forEach(hospital => {
      const marker = L.marker(hospital.pos, { icon: hospitalIcon })
        .addTo(mapInstanceRef.current!)
        .bindPopup(`
          <div style="text-align: center; min-width: 200px;">
            <strong>${hospital.name}</strong><br/>
            <small style="color: #666;">📍 ${hospital.distance} away</small><br/>
            <small style="color: #666;">📞 ${hospital.phone}</small>
          </div>
        `);
      markersRef.current.push(marker);
    });

    // Add ambulances
    ambulances.forEach(ambulance => {
      const marker = L.marker(ambulance.pos, { icon: ambulanceIcon })
        .addTo(mapInstanceRef.current!)
        .bindPopup(`
          <div style="text-align: center; min-width: 200px;">
            <strong>Ambulance ${ambulance.id.toUpperCase()}</strong><br/>
            <small style="color: #666;">📍 Location: ${ambulance.pos[0].toFixed(4)}, ${ambulance.pos[1].toFixed(4)}</small><br/>
            <button 
              onclick="window.bookAmbulance('${ambulance.id}')" 
              style="
                background: #059669; 
                color: white; 
                border: none; 
                padding: 8px 16px; 
                border-radius: 6px; 
                cursor: pointer; 
                margin-top: 8px;
                font-weight: 500;
              "
            >
              🚑 Book Ambulance
            </button>
          </div>
        `);
      markersRef.current.push(marker);
    });

    // Set up global booking function
    (window as any).bookAmbulance = (ambulanceId: string) => {
      const ambulance = ambulances.find(a => a.id === ambulanceId);
      if (ambulance) {
        onAmbulanceBook(ambulance);
      }
    };

  }, [userPos, hospitals, ambulances, onAmbulanceBook]);

  // Update route path
  useEffect(() => {
    if (!mapInstanceRef.current) return;

    // Remove existing route
    if (routeLineRef.current) {
      mapInstanceRef.current.removeLayer(routeLineRef.current);
      routeLineRef.current = null;
    }

    // Add new route
    if (routePath && routePath.length > 1) {
      routeLineRef.current = L.polyline(routePath, {
        color: '#2563eb',
        weight: 4,
        opacity: 0.8
      }).addTo(mapInstanceRef.current);

      // Fit map to show route
      mapInstanceRef.current.fitBounds(routeLineRef.current.getBounds(), { padding: [20, 20] });
    }
  }, [routePath]);

  // Center map on user location when it changes
  useEffect(() => {
    if (mapInstanceRef.current && userPos) {
      mapInstanceRef.current.setView(userPos, 13);
    }
  }, [userPos]);

  return (
    <div
      ref={mapRef}
      className="h-full w-full rounded-xl overflow-hidden min-h-[300px] md:min-h-[400px]"
    />
  );
}

function distance(a: [number, number], b: [number, number]) {
  const dLat = a[0] - b[0];
  const dLng = a[1] - b[1];
  return Math.hypot(dLat, dLng);
}

function stepTowards(curr: [number, number], target: [number, number], step = 0.002): [number, number] {
  const dLat = target[0] - curr[0];
  const dLng = target[1] - curr[1];
  const dist = Math.hypot(dLat, dLng);
  if (dist === 0 || dist <= step) return target;
  const r = step / dist;
  return [curr[0] + dLat * r, curr[1] + dLng * r];
}

export default function AmbulanceServices() {
  // Standard emergency numbers for India
  const EMERGENCY_NUMBERS = {
    ambulance: '102',
    police: '100',
    fire: '101'
  };

  const { isCollapsed, setIsCollapsed } = useSidebar();
  const [userPos, setUserPos] = useState<[number, number] | null>(null);
  const [isMoving, setIsMoving] = useState(false);
  const [phase, setPhase] = useState<"to-user" | "to-hospital" | "arrived">("to-user");
  const timerRef = useRef<number | null>(null);

  // Multi-ambulance state and active selection
  const [ambulances, setAmbulances] = useState<{ id: string; pos: [number, number]; base: [number, number] }[]>([]);
  const [selectedAmbId, setSelectedAmbId] = useState<string | null>(null);
  const [destination, setDestination] = useState<[number, number] | null>(null);
  const [routeIndex, setRouteIndex] = useState(0);

  // Dynamic emergency contacts (persisted locally)
  const [contacts, setContacts] = useState<{ name: string; phone: string }[]>([
    { name: "Riya Sharma", phone: "+91 98765 43210" },
    { name: "Arjun Verma", phone: "+91 91234 56780" },
    { name: "Family Group", phone: "+91 90000 00000" },
  ]);
  const [newContact, setNewContact] = useState({ name: "", phone: "" });

  useEffect(() => {
    try {
      const raw = localStorage.getItem("emergencyContacts");
      if (raw) setContacts(JSON.parse(raw));
    } catch { }
  }, []);
  useEffect(() => {
    try {
      localStorage.setItem("emergencyContacts", JSON.stringify(contacts));
    } catch { }
  }, [contacts]);

  // Road-aligned route (OSRM). Fallback to straight if unavailable.
  const [roadRoute, setRoadRoute] = useState<[number, number][] | null>(null);
  const recomputeRoute = useCallback(async (start: [number, number], mid: [number, number], end: [number, number]) => {
    try {
      const coords = [start, mid, end]
        .map((p) => `${p[1]},${p[0]}`) // lon,lat
        .join(";");
      const url = `https://router.project-osrm.org/route/v1/driving/${coords}?overview=full&geometries=geojson`;
      const res = await fetch(url);
      if (!res.ok) throw new Error("routing failed");
      const data = await res.json();
      const points: [number, number][] = data.routes[0].geometry.coordinates.map((c: [number, number]) => [c[1], c[0]]);
      setRoadRoute(points);
    } catch (e) {
      setRoadRoute(null);
    }
  }, []);

  // Enhanced geolocation with permission request
  const requestLocation = useCallback(() => {
    if (!navigator.geolocation) {
      alert("Geolocation is not supported by this browser.");
      setUserPos([22.7205, 75.8571]); // fallback near hospital
      return;
    }

    const options = {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 60000
    };

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setUserPos([pos.coords.latitude, pos.coords.longitude]);
        console.log("📍 Location obtained:", pos.coords.latitude, pos.coords.longitude);
      },
      (error) => {
        console.error("Location error:", error);
        switch (error.code) {
          case error.PERMISSION_DENIED:
            alert("Location access denied. Please enable location services and refresh the page.");
            break;
          case error.POSITION_UNAVAILABLE:
            alert("Location information unavailable.");
            break;
          case error.TIMEOUT:
            alert("Location request timed out.");
            break;
        }
        setUserPos([22.7205, 75.8571]); // fallback
      },
      options
    );
  }, []);

  // Request location on component mount
  useEffect(() => {
    requestLocation();
  }, [requestLocation]);


  // Enhanced ambulance movement with road-based routing
  useEffect(() => {
    if (!isMoving || !selectedAmbId || !userPos || !roadRoute || roadRoute.length < 2) return;

    const moveAmbulance = () => {
      setRouteIndex((currentIndex) => {
        if (currentIndex >= roadRoute.length - 1) {
          // Reached destination
          setIsMoving(false);
          setPhase("arrived");
          alert("🚑 Ambulance has arrived at your location!");
          return currentIndex;
        }

        // Move to next route point (slower progression)
        const nextIndex = Math.min(currentIndex + 1, roadRoute.length - 1);
        const nextPos = roadRoute[nextIndex];

        // Update ambulance position
        setAmbulances((list) => {
          return list.map((ambulance) => {
            if (ambulance.id !== selectedAmbId) return ambulance;
            return { ...ambulance, pos: nextPos };
          });
        });

        return nextIndex;
      });
    };

    const interval = setInterval(moveAmbulance, 3000); // Move every 3 seconds (slower)
    return () => clearInterval(interval);
  }, [isMoving, selectedAmbId, userPos, roadRoute]);

  // Generate route path for visualization using road route
  const routePath = useMemo(() => {
    if (!selectedAmbId || !roadRoute) return null;

    // Use the actual road route for visualization
    return roadRoute;
  }, [selectedAmbId, roadRoute]);

  // Make.com webhook integration for sending ambulance tracking link to all emergency contacts on dispatch
  const sendTrackingLinkToContacts = useCallback(async (bookingId: string) => {
    if (!userPos || contacts.length === 0) return;

    const trackingLink = `${window.location.origin}/ambulance-services?tracking=${bookingId}&lat=${userPos[0].toFixed(6)}&lng=${userPos[1].toFixed(6)}`;

    try {
      const response = await fetch('https://hook.eu2.make.com/qc7vltpprummrcvn69ybvibfvyet4xvw', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Agent': 'HealthSaarthi-AmbulanceDispatch/1.0'
        },
        body: JSON.stringify({
          action: 'ambulance_dispatched_tracking_link',
          source: 'HealthSaarthi-AmbulanceServices',
          booking_id: bookingId,
          tracking_link: trackingLink,
          message: `\ud83d\ude91 An ambulance has been dispatched for the HealthSaarthi user. Track live location here: ${trackingLink}`,
          location: `${userPos[0]}, ${userPos[1]}`,
          coordinates: { latitude: userPos[0], longitude: userPos[1] },
          timestamp: new Date().toISOString(),
          emergency_contacts: contacts.map(c => ({ name: c.name, phone: c.phone })),
          emergency_contacts_count: contacts.length,
        })
      });

      if (response.ok) {
        console.log('\u2705 Tracking link sent to all emergency contacts via Make.com');
        return true;
      } else {
        console.error('\u274c Tracking link webhook failed:', response.status, response.statusText);
        return false;
      }
    } catch (error) {
      console.error('\u274c Tracking link webhook error:', error);
      return false;
    }
  }, [userPos, contacts]);

  const startAmbulance = useCallback(async (amb: { id: string; pos: [number, number]; base: [number, number] }) => {
    if (!userPos) {
      alert("Fetching your location. Please allow location access and try again.");
      return;
    }

    const requestId = `REQ-${Date.now()}`;

    console.log("🚑 Starting ambulance:", amb.id);
    setSelectedAmbId(amb.id);
    setDestination(userPos);
    setRouteIndex(0);
    setRoadRoute(null); // Clear previous route

    // Show loading state
    alert("🛣️ Calculating optimal route...");

    // Fetch road route from ambulance to user
    try {
      const coords = `${amb.pos[1]},${amb.pos[0]};${userPos[1]},${userPos[0]}`;
      const url = `https://router.project-osrm.org/route/v1/driving/${coords}?overview=full&geometries=geojson&steps=true`;

      console.log("🛣️ Fetching route:", url);
      const res = await fetch(url);

      if (!res.ok) throw new Error("routing failed");

      const data = await res.json();
      const points: [number, number][] = data.routes[0].geometry.coordinates.map((c: [number, number]) => [c[1], c[0]]);

      console.log("✅ Route fetched:", points.length, "points");
      console.log("📊 Route distance:", (data.routes[0].distance / 1000).toFixed(2), "km");
      console.log("⏱️ Estimated time:", Math.round(data.routes[0].duration / 60), "minutes");

      setRoadRoute(points);
      setIsMoving(true);

      // Broadcast to driver dashboard
      const channel = new BroadcastChannel('ambulance_booking');
      channel.postMessage({
        type: 'NEW_REQUEST',
        payload: {
          id: requestId,
          pickupLocation: `User Location (${userPos[0].toFixed(4)}, ${userPos[1].toFixed(4)})`,
          distance: `${(data.routes[0].distance / 1000).toFixed(1)} km`,
          eta: `${Math.round(data.routes[0].duration / 60)} mins`,
          patientName: "Aditya Kumrawat",
          contactNumber: "+91 98765 43210"
        }
      });
      setTimeout(() => channel.close(), 1000); // Close after a short delay to ensure message is sent

      alert(`🚑 Ambulance dispatched! Route: ${(data.routes[0].distance / 1000).toFixed(1)}km, ETA: ${Math.round(data.routes[0].duration / 60)} min`);

      // Send shareable tracking link to all emergency contacts via SMS
      const sentOk = await sendTrackingLinkToContacts(requestId);
      if (sentOk) {
        setTimeout(() => alert(`📲 Tracking link sent to ${contacts.length} emergency contact${contacts.length !== 1 ? 's' : ''} via SMS.`), 800);
      }

    } catch (error) {
      console.error("❌ Route fetch failed:", error);
      // Fallback to direct route with more points for smoother movement
      const directPoints: [number, number][] = [];
      const steps = 20;
      for (let i = 0; i <= steps; i++) {
        const ratio = i / steps;
        const lat = amb.pos[0] + (userPos[0] - amb.pos[0]) * ratio;
        const lng = amb.pos[1] + (userPos[1] - amb.pos[1]) * ratio;
        directPoints.push([lat, lng]);
      }

      setRoadRoute(directPoints);
      setIsMoving(true);

      // Broadcast to driver dashboard (Fallback)
      const channel = new BroadcastChannel('ambulance_booking');
      const dist = distance(amb.pos, userPos) * 111; // approx km
      channel.postMessage({
        type: 'NEW_REQUEST',
        payload: {
          id: requestId,
          pickupLocation: `User Location (${userPos[0].toFixed(4)}, ${userPos[1].toFixed(4)})`,
          distance: `${dist.toFixed(1)} km`,
          eta: `${Math.round(dist * 2)} mins`,
          patientName: "Aditya Kumrawat",
          contactNumber: "+91 98765 43210"
        }
      });
      setTimeout(() => channel.close(), 1000);

      alert("🚑 Ambulance dispatched! (Using direct route)");

      // Send shareable tracking link to all emergency contacts via SMS
      const sentOk = await sendTrackingLinkToContacts(requestId);
      if (sentOk) {
        setTimeout(() => alert(`📲 Tracking link sent to ${contacts.length} emergency contact${contacts.length !== 1 ? 's' : ''} via SMS.`), 800);
      }
    }
  }, [userPos, contacts, sendTrackingLinkToContacts]);

  const onBookNearest = useCallback(() => {
    if (!userPos || ambulances.length === 0) {
      alert("Fetching your location or ambulances unavailable.");
      return;
    }
    const nearest = ambulances.reduce((best, a) => (distance(a.pos, userPos) < distance(best.pos, userPos) ? a : best), ambulances[0]);
    startAmbulance(nearest);
  }, [userPos, ambulances, startAmbulance]);

  const onCancel = useCallback(() => {
    setIsMoving(false);
    setRouteIndex(0);
    setPhase("to-user");
    setRoadRoute(null);
    if (selectedAmbId) {
      setAmbulances((list) => list.map((a) => (a.id === selectedAmbId ? { ...a, pos: a.base } : a)));
    }
    setSelectedAmbId(null);
    setDestination(null);
  }, [selectedAmbId]);

  const nearestHospitals = useMemo(() => {
    const base = userPos ?? HOSPITAL_POS;
    const hospitalData = [
      { name: "Indore Central Hospital", offset: [0, 0], distance: "0.5 km", phone: "+91 98765 00000" },
      { name: "City General Hospital", offset: [0.005, 0.004], distance: "1.2 km", phone: "+91 98765 11111" },
      { name: "Apollo Medical Center", offset: [-0.004, 0.006], distance: "1.8 km", phone: "+91 98765 22222" },
      { name: "Fortis Healthcare", offset: [0.006, -0.003], distance: "2.1 km", phone: "+91 98765 33333" },
      { name: "Max Super Specialty", offset: [-0.003, -0.004], distance: "2.5 km", phone: "+91 98765 44444" },
      { name: "Medanta Hospital", offset: [0.007, 0.002], distance: "2.8 km", phone: "+91 98765 55555" },
      { name: "Lifepoint Multispecialty", offset: [-0.005, -0.005], distance: "3.0 km", phone: "+91 98765 66666" },
      { name: "CHL Hospital", offset: [0.004, -0.006], distance: "3.2 km", phone: "+91 98765 77777" },
      { name: "Bombay Hospital", offset: [-0.006, 0.003], distance: "3.5 km", phone: "+91 98765 88888" },
      { name: "Greater Kailash Hospital", offset: [0.008, -0.004], distance: "3.8 km", phone: "+91 98765 99999" },
    ];
    return hospitalData.map((h, i) => ({
      id: `h${i}`,
      name: h.name,
      pos: [base[0] + h.offset[0], base[1] + h.offset[1]] as [number, number],
      distance: h.distance,
      phone: h.phone,
    }));
  }, [userPos]);

  const hospitals = useMemo(() => [HOSPITAL_POS, ...nearestHospitals.map((h) => h.pos)], [nearestHospitals]);
  useEffect(() => {
    if (ambulances.length === 0 && hospitals.length) {
      const list = hospitals.slice(0, 7).map((hp, i) => ({
        id: `amb${i + 1}`,
        base: hp,
        pos: [hp[0] + (i % 2 ? 0.001 : -0.001), hp[1] + (i % 3 ? 0.0015 : -0.0015)] as [number, number],
      }));
      setAmbulances(list);
    }
  }, [hospitals, ambulances.length]);

  // Make.com webhook integration for individual call button presses
  const sendCallToWebhook = useCallback(async (phoneNumber: string) => {
    try {
      const response = await fetch('https://hook.eu2.make.com/73flkastt4wnj4q8do6p9snr34a0ipnq', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Agent': 'HealthSaarthi-CallButton/1.0'
        },
        body: JSON.stringify({
          phone: phoneNumber,
          timestamp: new Date().toISOString(),
          action: 'call_button_pressed',
          source: 'HealthSaarthi-AmbulanceServices'
        })
      });

      if (response.ok) {
        console.log('✅ Call button press sent to webhook successfully');
        return true;
      } else {
        console.error('❌ Call webhook request failed:', response.status, response.statusText);
        return false;
      }
    } catch (error) {
      console.error('❌ Call webhook error:', error);
      return false;
    }
  }, []);

  // Make.com webhook integration for adding contacts
  const sendContactToWebhook = useCallback(async (contactData: { name: string; phone: string }) => {
    try {
      const response = await fetch('https://hook.eu2.make.com/qbub2v40xyb9dnky2uokr6ajdja384li', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Agent': 'HealthSaarthi-ContactAdd/1.0'
        },
        body: JSON.stringify({
          name: contactData.name,
          phone: contactData.phone,
          timestamp: new Date().toISOString(),
          action: 'contact_added',
          source: 'HealthSaarthi-AmbulanceServices'
        })
      });

      if (response.ok) {
        console.log('✅ Contact sent to Make.com webhook successfully');
        return true;
      } else {
        console.error('❌ Webhook request failed:', response.status, response.statusText);
        return false;
      }
    } catch (error) {
      console.error('❌ Webhook error:', error);
      return false;
    }
  }, []);

  // Make.com webhook integration for deleting contacts
  const sendDeleteToWebhook = useCallback(async (phoneNumber: string) => {
    try {
      const response = await fetch('https://hook.eu2.make.com/qxbbhti8fm3dd03gxw4ectmpc8v26kmh', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Agent': 'HealthSaarthi-ContactDelete/1.0'
        },
        body: JSON.stringify({
          phone: phoneNumber,
          timestamp: new Date().toISOString(),
          action: 'contact_deleted',
          source: 'HealthSaarthi-AmbulanceServices'
        })
      });

      if (response.ok) {
        console.log('✅ Contact deletion sent to Make.com webhook successfully');
        return true;
      } else {
        console.error('❌ Delete webhook request failed:', response.status, response.statusText);
        return false;
      }
    } catch (error) {
      console.error('❌ Delete webhook error:', error);
      return false;
    }
  }, []);

  // Make.com webhook integration for notifying individual contact
  const sendNotifyToWebhook = useCallback(async (contactData: { name: string; phone: string }) => {
    if (!userPos) {
      alert("Location not available. Please enable location services.");
      return false;
    }

    try {
      const response = await fetch('https://hook.eu2.make.com/ybq6snwdy5k6etm8ntd96nq1ou33nawt', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Agent': 'HealthSaarthi-EmergencyNotify/1.0'
        },
        body: JSON.stringify({
          name: contactData.name,
          phone: contactData.phone,
          location: `${userPos[0]}, ${userPos[1]}`,
          coordinates: {
            latitude: userPos[0],
            longitude: userPos[1]
          },
          time: new Date().toISOString(),
          timestamp: new Date().toISOString(),
          action: 'emergency_notify',
          source: 'HealthSaarthi-AmbulanceServices'
        })
      });

      if (response.ok) {
        console.log('✅ Emergency notification sent to Make.com webhook successfully');
        alert(`🔔 Emergency notification sent to ${contactData.name}`);
        return true;
      } else {
        console.error('❌ Notify webhook request failed:', response.status, response.statusText);
        alert('Failed to send notification. Please try again.');
        return false;
      }
    } catch (error) {
      console.error('❌ Notify webhook error:', error);
      alert('Failed to send notification. Please check your connection.');
      return false;
    }
  }, [userPos]);

  // Make.com webhook integration for call all button
  const sendCallAllToWebhook = useCallback(async () => {
    if (!userPos) {
      alert("Location not available. Please enable location services.");
      return false;
    }

    try {
      const response = await fetch('https://hook.eu2.make.com/2b9qvf5zvmu99zss88yemjgl11irj4nw', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Agent': 'HealthSaarthi-CallAll/1.0'
        },
        body: JSON.stringify({
          location: `${userPos[0]}, ${userPos[1]}`,
          coordinates: {
            latitude: userPos[0],
            longitude: userPos[1]
          },
          emergency_contacts_count: contacts.length,
          timestamp: new Date().toISOString(),
          action: 'call_all_pressed',
          source: 'HealthSaarthi-AmbulanceServices'
        })
      });

      if (response.ok) {
        console.log('✅ Call All button press sent to Make.com webhook successfully');
        return true;
      } else {
        console.error('❌ Call All webhook request failed:', response.status, response.statusText);
        return false;
      }
    } catch (error) {
      console.error('❌ Call All webhook error:', error);
      return false;
    }
  }, [userPos, contacts.length]);

  // Make.com webhook integration for notify all button
  const sendNotifyAllToWebhook = useCallback(async () => {
    if (!userPos) {
      console.warn("Location not available for Notify All webhook");
      return false;
    }

    try {
      const response = await fetch('https://hook.eu2.make.com/qc7vltpprummrcvn69ybvibfvyet4xvw', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Agent': 'HealthSaarthi-NotifyAll/1.0'
        },
        body: JSON.stringify({
          location: `${userPos[0]}, ${userPos[1]}`,
          coordinates: {
            latitude: userPos[0],
            longitude: userPos[1]
          },
          time: new Date().toISOString(),
          emergency_contacts_count: contacts.length,
          action: 'notify_all_pressed',
          source: 'HealthSaarthi-AmbulanceServices'
        })
      });

      if (response.ok) {
        console.log('✅ Notify All button press sent to webhook successfully');
        return true;
      } else {
        console.error('❌ Notify All webhook request failed:', response.status, response.statusText);
        return false;
      }
    } catch (error) {
      console.error('❌ Notify All webhook error:', error);
      return false;
    }
  }, [userPos, contacts.length]);

  return (
    <div className="dashboard-page min-h-screen bg-gradient-to-br from-white via-[#f8fbff] to-[#eef2ff]">
      <FloatingSidebar isCollapsed={isCollapsed} setIsCollapsed={setIsCollapsed} />
      <FloatingTopBar isCollapsed={isCollapsed} />
      <div className={`transition-all duration-300 ${isCollapsed ? "md:ml-20" : "md:ml-72"} ml-0 pt-20 md:pt-28 px-4 md:px-0`}>
        <div className="mx-auto w-full max-w-7xl px-2 md:px-6 pb-16">
          <h1 className="dashboard-title text-xl md:text-3xl font-extrabold tracking-tight text-gray-900 mb-5 ml-14 md:ml-0">Emergency Services</h1>

          <div className="grid grid-cols-1 xl:grid-cols-10 gap-6">
            {/* Left: 70% (xl: col-span-6) */}
            <div className="xl:col-span-6 space-y-4">
              {/* Primary Buttons Row */}
              <div className="flex items-center gap-3 w-full">
                <Button onClick={onBookNearest} className="flex-1 bg-[#DE3C3A] hover:bg-red-700 h-12 rounded-2xl text-[15px] font-semibold text-white shadow-sm border-none">
                  <Siren className="mr-2 h-4 w-4" /> Book
                </Button>
                <Button className="flex-1 bg-[#E86C1D] hover:bg-orange-600 h-12 rounded-2xl text-[15px] font-semibold text-white shadow-sm border-none" onClick={() => window.location.href = `tel:${EMERGENCY_NUMBERS.ambulance}`}>
                  <Phone className="mr-2 h-4 w-4 fill-current" /> Call 112
                </Button>
              </div>

              {/* Status Row */}
              <div className="flex items-center justify-between w-full">
                <div className="flex items-center gap-2">
                  {userPos ? (
                    <div className="flex items-center gap-2 text-[13px] font-medium text-green-600 bg-green-50/80 px-3 py-1.5 rounded-full border border-green-100">
                      <div className="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse"></div>
                      Location Active
                    </div>
                  ) : (
                    <Button onClick={requestLocation} variant="outline" size="sm" className="bg-blue-50 h-8 text-xs rounded-full">
                      <MapPin className="mr-1.5 h-3 w-3" /> Get Location
                    </Button>
                  )}
                  {isMoving && selectedAmbId && (
                    <div className="flex items-center gap-2 text-[13px] font-medium text-blue-600 bg-blue-50/80 px-3 py-1.5 rounded-full border border-blue-100">
                      <div className="w-1.5 h-1.5 bg-blue-500 rounded-full animate-pulse"></div>
                      Ambulance En Route
                    </div>
                  )}
                </div>
                {(isMoving || selectedAmbId || userPos) && (
                  <Button variant="ghost" size="sm" onClick={onCancel} className="text-gray-500 h-8 text-xs underline underline-offset-2 hover:bg-transparent px-2">
                    Cancel Request
                  </Button>
                )}
              </div>

              <div className="h-[300px] md:h-[400px] rounded-2xl overflow-hidden border border-gray-100 shadow-sm bg-white/90 relative z-0">
                <LeafletMap
                  userPos={userPos}
                  hospitals={nearestHospitals}
                  ambulances={ambulances}
                  onAmbulanceBook={startAmbulance}
                  routePath={routePath}
                />
              </div>

              {/* Nearby Hospitals List */}
              <Card className="rounded-[32px] border-none bg-white shadow-sm mt-2 relative z-10 w-full mb-6 py-2">
                <CardHeader className="pb-3 px-6 pt-5">
                  <CardTitle className="dashboard-title text-lg font-bold tracking-tight flex items-center gap-3">
                    <div className="rounded-full border border-red-200 p-1 flex items-center justify-center">
                      <MapPin className="h-4 w-4 text-red-500" />
                    </div>
                    Hospitals ({nearestHospitals.length})
                  </CardTitle>
                </CardHeader>
                <CardContent className="px-5">
                  <div className="space-y-3 max-h-[350px] overflow-y-auto">
                    {nearestHospitals.map((hospital) => (
                      <div
                        key={hospital.id}
                        className="flex items-center justify-between rounded-[24px] border border-gray-100 bg-white p-3 shadow-sm hover:shadow-md transition-all"
                      >
                        <div className="flex items-center gap-3 flex-1 overflow-hidden pr-2">
                          <div className="flex items-center justify-center w-10 h-10 rounded-full bg-red-50 text-red-600 text-sm font-bold shrink-0">
                            ✚
                          </div>
                          <div className="truncate">
                            <h3 className="font-bold text-[15px] truncate">{hospital.name}</h3>
                            <p className="text-xs text-gray-500 flex items-center gap-1 mt-0.5 font-medium">
                              <Navigation className="h-3 w-3" />
                              {hospital.distance}
                            </p>
                          </div>
                        </div>
                        <div className="flex items-center gap-2 shrink-0">
                          <button
                            className="flex items-center justify-center w-10 h-10 rounded-full bg-[#1e9a4b] text-white hover:bg-green-700 transition-colors shadow-sm"
                            onClick={() => {
                              window.location.href = `tel:${hospital.phone}`;
                            }}
                          >
                            <Phone className="h-4 w-4 fill-current" />
                          </button>
                          <button
                            className="flex items-center justify-center w-10 h-10 rounded-full border border-gray-200 bg-white text-gray-700 hover:bg-gray-50 transition-colors shadow-sm"
                            onClick={() => {
                              window.open(
                                `https://www.google.com/maps/dir/?api=1&destination=${hospital.pos[0]},${hospital.pos[1]}`,
                                '_blank'
                              );
                            }}
                          >
                            <Navigation className="h-4 w-4" />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Right: 30% (xl: col-span-4) */}
            <div className="xl:col-span-4 space-y-4">
              {/* Emergency Services */}
              <Card className={frostedCardClass}>
                <CardHeader>
                  <CardTitle className="dashboard-title text-lg font-semibold tracking-tight">Emergency Services</CardTitle>
                </CardHeader>
                <CardContent className="grid grid-cols-3 gap-2 sm:gap-3">
                  <Button className="flex flex-col items-center gap-1 py-4 sm:py-6 px-1 sm:px-4" onClick={() => window.location.href = `tel:${EMERGENCY_NUMBERS.ambulance}`}>
                    <AmbulanceIcon className="h-5 w-5" />
                    <span className="text-[10px] sm:text-xs">Ambulance</span>
                  </Button>
                  <Button className="flex flex-col items-center gap-1 py-4 sm:py-6 px-1 sm:px-4" onClick={() => window.location.href = `tel:${EMERGENCY_NUMBERS.police}`}>
                    <ShieldAlert className="h-5 w-5" />
                    <span className="text-[10px] sm:text-xs">Police</span>
                  </Button>
                  <Button className="flex flex-col items-center gap-1 py-4 sm:py-6 px-1 sm:px-4" onClick={() => window.location.href = `tel:${EMERGENCY_NUMBERS.fire}`}>
                    <FlameKindling className="h-5 w-5" />
                    <span className="text-[10px] sm:text-xs">Fire</span>
                  </Button>
                </CardContent>
              </Card>

              {/* Emergency Contacts */}
              <Card className={frostedCardClass}>
                <CardHeader>
                  <CardTitle className="dashboard-title text-lg font-semibold tracking-tight">Emergency Contacts</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  {/* Add contact form */}
                  <div className="rounded-xl border bg-white/70 p-3">
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                      <input
                        type="text"
                        placeholder="Name"
                        value={newContact.name}
                        onChange={(e) => setNewContact((v) => ({ ...v, name: e.target.value }))}
                        className="col-span-1 sm:col-span-1 h-9 rounded-lg border px-3 text-sm"
                      />
                      <input
                        type="tel"
                        placeholder="Phone"
                        value={newContact.phone}
                        onChange={(e) => setNewContact((v) => ({ ...v, phone: e.target.value }))}
                        className="col-span-1 sm:col-span-1 h-9 rounded-lg border px-3 text-sm"
                      />
                      <Button
                        size="sm"
                        onClick={async () => {
                          const n = newContact.name.trim();
                          const p = newContact.phone.trim();
                          if (!n || !p) {
                            alert("Please enter name and phone");
                            return;
                          }
                          // Add contact to local state
                          setContacts((c) => [...c, { name: n, phone: p }]);
                          // Send to Make.com webhook
                          await sendContactToWebhook({ name: n, phone: p });
                          // Clear form
                          setNewContact({ name: "", phone: "" });
                        }}
                        className="w-full"
                      >
                        Add
                      </Button>
                    </div>
                  </div>

                  {/* Contacts list */}
                  {contacts.map((c, i) => (
                    <div key={`${c.name}-${i}`} className="flex items-center justify-between rounded-xl border bg-white/60 p-3">
                      <div>
                        <div className="text-sm font-medium">{c.name}</div>
                        <div className="text-xs text-muted-foreground">{c.phone}</div>
                      </div>
                      <div className="flex items-center gap-2">
                        <Button size="sm" className="bg-blue-600 text-white hover:bg-blue-700 border-blue-600" onClick={async () => {
                          // Send call button press to webhook only (no dialer redirect)
                          const success = await sendCallToWebhook(c.phone);
                          if (success) {
                            alert(`📞 Call initiated for ${c.name}`);
                          }
                        }}><Phone className="h-4 w-4" /></Button>
                        <Button size="sm" className="bg-blue-600 text-white hover:bg-blue-700 border-blue-600" onClick={async () => {
                          // Send emergency notification to webhook first, then call
                          await sendNotifyToWebhook({ name: c.name, phone: c.phone });
                        }}><Bell className="h-4 w-4" /></Button>
                        <Button size="sm" variant="secondary" onClick={async () => {
                          // Send to Make.com webhook before deleting
                          await sendDeleteToWebhook(c.phone);
                          // Remove contact from local state
                          setContacts((list) => list.filter((_, idx) => idx !== i));
                        }}><Trash2 className="h-4 w-4" /></Button>
                      </div>
                    </div>
                  ))}
                  <Button className="w-full" onClick={async () => {
                    // Send Notify All button press to webhook
                    const success = await sendNotifyAllToWebhook();
                    if (success) {
                      alert(`🚨 Emergency notification sent to ${contacts.length} contacts`);
                    }
                    // No dialer redirect - only webhook notification
                  }}>Notify All</Button>

                  <Button className="w-full" onClick={async () => {
                    // Send Call All button press to webhook first
                    const success = await sendCallAllToWebhook();
                    if (success) {
                      alert(`📞 Call All initiated for ${contacts.length} emergency contacts`);
                    }
                    // No dialer redirect - only webhook notification
                  }}>Call All</Button>
                </CardContent>
              </Card>

              {/* Call all emergency services */}
              <Card className={frostedCardClass}>
                <CardContent className="p-4">
                  <Button className="w-full bg-red-600 hover:bg-red-700 py-6 text-base" onClick={() => {
                    // Call all emergency services (112 is the common emergency number in India)
                    window.location.href = "tel:112";
                  }}>Call All Emergency Services</Button>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </div>

    </div>
  );
}
