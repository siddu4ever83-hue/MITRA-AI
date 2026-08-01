from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.button import Button
from kivy.uix.scrollview import ScrollView
from kivy.uix.anchorlayout import AnchorLayout
from kivy.graphics import Color, RoundedRectangle
from kivy.clock import Clock
from datetime import datetime
import threading
import requests
import json


GROQ_API_KEY = ""  # Free API key - groq.com


class MessageBubble(BoxLayout):
    def __init__(self, text, is_user=False, **kwargs):
        super().__init__(**kwargs)
        self.orientation = 'vertical'
        self.size_hint_y = None
        self.padding = [10, 5]

        anchor = AnchorLayout(
            anchor_x='right' if is_user else 'left',
            size_hint_y=None
        )

        bubble = Label(
            text=text,
            size_hint=(None, None),
            text_size=(280, None),
            halign='right' if is_user else 'left',
            valign='middle',
            padding=(12, 8),
            color=(1, 1, 1, 1),
            font_size=15
        )
        bubble.bind(texture_size=bubble.setter('size'))

        with bubble.canvas.before:
            if is_user:
                Color(0.2, 0.5, 1, 1)
            else:
                Color(0.15, 0.15, 0.25, 1)
            self.rect = RoundedRectangle(
                pos=bubble.pos,
                size=bubble.size,
                radius=[15]
            )

        bubble.bind(
            pos=lambda i, v: setattr(self.rect, 'pos', v),
            size=lambda i, v: setattr(self.rect, 'size', v)
        )

        anchor.add_widget(bubble)
        anchor.height = bubble.height + 20
        self.height = anchor.height
        self.add_widget(anchor)


class MitraChat(BoxLayout):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.orientation = 'vertical'
        self.spacing = 0

        with self.canvas.before:
            Color(0.05, 0.05, 0.15, 1)
            self.bg = RoundedRectangle(
                pos=self.pos,
                size=self.size
            )
        self.bind(
            pos=lambda i, v: setattr(self.bg, 'pos', v),
            size=lambda i, v: setattr(self.bg, 'size', v)
        )

        # Header
        header = BoxLayout(
            size_hint_y=None,
            height=65,
            padding=[15, 10]
        )
        with header.canvas.before:
            Color(0.1, 0.1, 0.3, 1)
            self.header_bg = RoundedRectangle(
                pos=header.pos,
                size=header.size
            )
        header.bind(
            pos=lambda i, v: setattr(self.header_bg, 'pos', v),
            size=lambda i, v: setattr(self.header_bg, 'size', v)
        )

        header_text = BoxLayout(orientation='vertical')
        header_text.add_widget(Label(
            text='🤖 MITRA AI',
            font_size=22,
            bold=True,
            color=(0.3, 0.7, 1, 1),
            halign='left'
        ))
        header_text.add_widget(Label(
            text='Your Personal AI Assistant',
            font_size=12,
            color=(0.6, 0.6, 0.8, 1),
            halign='left'
        ))
        header.add_widget(header_text)
        self.add_widget(header)

        # Chat Area
        self.scroll = ScrollView(size_hint_y=0.82)
        self.chat_layout = BoxLayout(
            orientation='vertical',
            size_hint_y=None,
            spacing=5,
            padding=[10, 10]
        )
        self.chat_layout.bind(
            minimum_height=self.chat_layout.setter('height')
        )
        self.scroll.add_widget(self.chat_layout)
        self.add_widget(self.scroll)

        # Input Area
        input_area = BoxLayout(
            size_hint_y=None,
            height=60,
            padding=[10, 8],
            spacing=8
        )
        with input_area.canvas.before:
            Color(0.1, 0.1, 0.25, 1)
            self.input_bg = RoundedRectangle(
                pos=input_area.pos,
                size=input_area.size
            )
        input_area.bind(
            pos=lambda i, v: setattr(self.input_bg, 'pos', v),
            size=lambda i, v: setattr(self.input_bg, 'size', v)
        )

        self.user_input = TextInput(
            hint_text='Ask MITRA anything...',
            multiline=False,
            size_hint_x=0.8,
            background_color=(0.15, 0.15, 0.3, 1),
            foreground_color=(1, 1, 1, 1),
            hint_text_color=(0.5, 0.5, 0.7, 1),
            cursor_color=(0.3, 0.7, 1, 1),
            font_size=16,
            padding=[10, 10]
        )
        self.user_input.bind(
            on_text_validate=self.send_message
        )

        send_btn = Button(
            text='▶',
            size_hint_x=0.2,
            font_size=20,
            background_color=(0.2, 0.5, 1, 1),
            color=(1, 1, 1, 1)
        )
        send_btn.bind(on_press=self.send_message)

        input_area.add_widget(self.user_input)
        input_area.add_widget(send_btn)
        self.add_widget(input_area)

        # Welcome Message
        Clock.schedule_once(lambda dt: self.add_mitra_message(
            "Namaskaram! 👋 Nenu MITRA AI - Mee Personal Assistant!\n\n"
            "Nenu meeru cheppindi anni cheyagalanu:\n"
            "🌤 Weather cheppagalanu\n"
            "💡 Questions answer cheyagalanu\n"
            "📅 Date & Time cheppagalanu\n"
            "🧮 Calculations cheyagalanu\n\n"
            "Em help kavali?"
        ), 0.5)

    def add_user_message(self, text):
        bubble = MessageBubble(text=text, is_user=True)
        self.chat_layout.add_widget(bubble)
        Clock.schedule_once(lambda dt: setattr(
            self.scroll, 'scroll_y', 0), 0.1)

    def add_mitra_message(self, text):
        bubble = MessageBubble(text=text, is_user=False)
        self.chat_layout.add_widget(bubble)
        Clock.schedule_once(lambda dt: setattr(
            self.scroll, 'scroll_y', 0), 0.1)

    def send_message(self, instance):
        text = self.user_input.text.strip()
        if not text:
            return
        self.add_user_message(text)
        self.user_input.text = ''
        self.add_mitra_message("MITRA thinking... 🤔")
        threading.Thread(
            target=self.get_ai_response,
            args=(text,),
            daemon=True
        ).start()

    def get_ai_response(self, text):
        # Local responses
        response = self.local_response(text)
        if response:
            Clock.schedule_once(
                lambda dt: self.update_last_message(response), 0)
            return

        # Groq AI API
        try:
            headers = {
                "Authorization": f"Bearer {GROQ_API_KEY}",
                "Content-Type": "application/json"
            }
            data = {
                "model": "llama3-8b-8192",
                "messages": [
                    {
                        "role": "system",
                        "content": (
                            "You are MITRA AI, a helpful Telugu/English "
                            "assistant like JARVIS. Be friendly, smart "
                            "and helpful. Keep answers short and clear."
                        )
                    },
                    {"role": "user", "content": text}
                ],
                "max_tokens": 500
            }
            res = requests.post(
                "https://api.groq.com/openai/v1/chat/completions",
                headers=headers,
                json=data,
                timeout=15
            )
            result = res.json()
            reply = result['choices'][0]['message']['content']
            Clock.schedule_once(
                lambda dt: self.update_last_message(reply), 0)
        except Exception as e:
            Clock.schedule_once(
                lambda dt: self.update_last_message(
                    "Sorry! Internet connection ledu. "
                    "Please check cheyandi! 📶"
                ), 0)

    def update_last_message(self, text):
        if self.chat_layout.children:
            self.chat_layout.remove_widget(
                self.chat_layout.children[0]
            )
        self.add_mitra_message(text)

    def local_response(self, text):
        text = text.lower()
        now = datetime.now()

        if any(w in text for w in ['time', 'time enti', 'samayam']):
            return f"⏰ Ippudu time: {now.strftime('%I:%M %p')}"

        elif any(w in text for w in ['date', 'today', 'indu']):
            return f"📅 Indu date: {now.strftime('%d %B %Y')}"

        elif any(w in text for w in ['hello', 'hi', 'namaste', 'hey']):
            return "Hello! 👋 Ela unnaru? Em help kavali?"

        elif any(w in text for w in ['name', 'peru', 'who are you']):
            return "Nenu MITRA AI! 🤖 Mee personal assistant!"

        elif any(w in text for w in ['bye', 'goodbye', 'thanks']):
            return "Bye! 👋 Manchiga undandi! Meeru anytime ravalchu!"

        elif any(w in text for w in ['joke', 'jokes', 'funny']):
            return (
                "😄 Oka joke
