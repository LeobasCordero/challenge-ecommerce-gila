package app.authz

import future.keywords.if

default allow = false

# Helper to check if a user is an admin
is_admin if {
    input.roles[_] == "ROLE_ADMIN"
}

# Helper to check if a user is a customer
is_customer if {
    input.roles[_] == "ROLE_CUSTOMER"
}

# Admin gets access to all endpoints and methods
allow if {
    is_admin
}

# Public endpoints (can be accessed by anyone, including guests)
# POST /api/v1/auth/login
allow if {
    input.method == "POST"
    input.path == ["api", "v1", "auth", "login"]
}

# GET /api/v1/products and GET /api/v1/products/{id} (public)
allow if {
    input.method == "GET"
    input.path[0] == "api"
    input.path[1] == "v1"
    input.path[2] == "products"
}

# Customer specific endpoints:
# POST /api/v1/orders/checkout
allow if {
    is_customer
    input.method == "POST"
    input.path == ["api", "v1", "orders", "checkout"]
}

# CRUD on /api/v1/cart and /api/v1/cart/items/**
allow if {
    is_customer
    input.path[0] == "api"
    input.path[1] == "v1"
    input.path[2] == "cart"
}

# POST /api/v1/chatbot/query
allow if {
    is_customer
    input.method == "POST"
    input.path == ["api", "v1", "chatbot", "query"]
}
