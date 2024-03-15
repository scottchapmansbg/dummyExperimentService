class Api {

    constructor(authToken) {
        this.authToken = authToken;
    }

    headers = {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    };

    BASE_URL = '/v2';
    EXPERIMENTS_URL = `${this.BASE_URL}/experiments`;
    ASSIGN_EXPERIMENT_URL = `${this.BASE_URL}/assign`;
    createHeaders() {
        return this.authToken ? {
            ...this.headers,
            'Authorization': 'Bearer ' + this.authToken
        } : this.headers;
    }

    async getAll() {
        return await fetch(this.EXPERIMENTS_URL, {
            method: 'GET',
            headers: this.createHeaders()
        });
    }

    async getById(id) {
        return await fetch(`${this.ASSIGN_EXPERIMENT_URL}/${id}`, {
            method: 'GET',
            headers: this.createHeaders()
        });
    }

    async delete(id) {
        return await fetch(`${this.EXPERIMENTS_URL}/${id}`, {
            method: 'DELETE',
            headers: this.createHeaders()
        });
    }

    async create(item) {
        return await fetch(this.EXPERIMENTS_URL, {
            method: 'POST',
            headers: this.createHeaders(),
            body: JSON.stringify(item)
        });
    }
}

export default Api;
